package teacommontea.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import teacommontea.util.DaemonExecutor;

public final class KvStore {

    private static final int MAGIC = 0x53565231;

    private static final byte T_STRING = 1;
    private static final byte T_LONG   = 2;
    private static final byte T_INT    = 3;
    private static final byte T_BOOL   = 4;
    private static final byte T_DEC    = 5;

    private static final int CAS_RETRIES = 32;

    private final String column;
    private final boolean shared;
    private final boolean readOnly;

    private final Map<String, TreeMap<String, Object>> scopes = new ConcurrentHashMap<>();

    private final Deque<Delta> journal = new ArrayDeque<>();

    private final java.util.concurrent.ExecutorService io = DaemonExecutor.single("KvStore-IO");

    private volatile boolean dirty;

    private KvStore(String column, boolean shared, boolean readOnly) {
        this.column = column;
        this.shared = shared;
        this.readOnly = readOnly;
    }

    public static KvStore open(String column) throws Exception {
        return open(column, false, false);
    }

    public static KvStore open(String column, boolean shared, boolean readOnly) throws Exception {
        KvStore s = new KvStore(column, shared, readOnly);
        s.ingest();
        return s;
    }

    public Scope scope(String name) {
        return new Scope(this, name);
    }

    void set(String scope, String key, Object value) {
        if (value == null) {
            delete(scope, key);
            return;
        }
        if (readOnly) {
            return;
        }
        synchronized (this) {
            scopes.computeIfAbsent(scope, k -> new TreeMap<>()).put(key, value);
            journal.addLast(Delta.set(scope, key, value));
        }
        markDirty();
    }

    void delete(String scope, String key) {
        if (readOnly) {
            return;
        }
        synchronized (this) {
            TreeMap<String, Object> m = scopes.get(scope);
            boolean present = m != null && m.containsKey(key);
            if (present) {
                m.remove(key);
                if (m.isEmpty()) {
                    scopes.remove(scope);
                }
            }
            journal.addLast(Delta.delete(scope, key));
        }
        markDirty();
    }

    Object get(String scope, String key) {
        freshen();
        TreeMap<String, Object> m = scopes.get(scope);
        return m == null ? null : m.get(key);
    }

    List<Scope.Entry> entries(String scope) {
        freshen();
        TreeMap<String, Object> m = scopes.get(scope);
        if (m == null) {
            return Collections.emptyList();
        }
        List<Scope.Entry> out = new ArrayList<>(m.size());
        for (Map.Entry<String, Object> e : m.entrySet()) {
            out.add(new Scope.Entry(e.getKey(), e.getValue()));
        }
        return out;
    }

    private void freshen() {
        if (!shared) {
            return;
        }
        synchronized (this) {
            ingestLocked();
        }
    }

    private void ingest() {
        synchronized (this) {
            ingestLocked();
        }
    }

    private void ingestLocked() {
        VeriteH2 db = VeriteH2.active();
        if (db == null) {
            return;
        }
        byte[] bytes = db.read(column);
        Map<String, TreeMap<String, Object>> base = deserialize(bytes);
        scopes.clear();
        scopes.putAll(base);
        for (Delta d : journal) {
            applyTo(scopes, d);
        }
    }

    private void markDirty() {
        dirty = true;
        io.execute(this::flushIfDirty);
    }

    private synchronized void flushIfDirty() {
        if (!dirty || readOnly) {
            return;
        }
        VeriteH2 db = VeriteH2.active();
        if (db == null) {
            return;
        }

        if (!shared) {
            byte[] bytes = serialize(scopes);
            if (bytes == null) {
                return;
            }
            db.write(column, bytes);
            dirty = false;
            return;
        }

        List<Delta> pending = new ArrayList<>(journal);
        if (pending.isEmpty()) {
            dirty = false;
            return;
        }
        for (int attempt = 0; attempt < CAS_RETRIES; attempt++) {
            byte[] expected = db.read(column);
            Map<String, TreeMap<String, Object>> base = deserialize(expected);
            for (Delta d : pending) {
                applyTo(base, d);
            }
            byte[] next = serialize(base);
            if (next == null) {
                return;
            }
            if (db.writeIf(column, expected, next)) {
                journal.removeAll(pending);
                scopes.clear();
                scopes.putAll(base);
                for (Delta d : journal) {
                    applyTo(scopes, d);
                }
                dirty = !journal.isEmpty();
                return;
            }
        }
        dirty = true;
    }

    public void shutdown() {
        flushIfDirty();
        io.shutdown();
    }

    private static void applyTo(Map<String, TreeMap<String, Object>> map, Delta d) {
        if (d.value != null) {
            map.computeIfAbsent(d.scope, k -> new TreeMap<>()).put(d.key, d.value);
        } else {
            TreeMap<String, Object> m = map.get(d.scope);
            if (m != null) {
                m.remove(d.key);
                if (m.isEmpty()) {
                    map.remove(d.scope);
                }
            }
        }
    }

    private static byte[] serialize(Map<String, TreeMap<String, Object>> map) {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buf)) {
            out.writeInt(MAGIC);
            out.writeInt(map.size());
            for (Map.Entry<String, TreeMap<String, Object>> s : map.entrySet()) {
                writeString(out, s.getKey());
                out.writeInt(s.getValue().size());
                for (Map.Entry<String, Object> kv : s.getValue().entrySet()) {
                    writeString(out, kv.getKey());
                    writeValue(out, kv.getValue());
                }
            }
            return buf.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, TreeMap<String, Object>> deserialize(byte[] bytes) {
        Map<String, TreeMap<String, Object>> out = new TreeMap<>();
        if (bytes == null) {
            return out;
        }
        try (DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(bytes))) {
            if (in.readInt() != MAGIC) {
                return out;
            }
            int scopeCount = in.readInt();
            for (int i = 0; i < scopeCount; i++) {
                String scope = readString(in);
                int keyCount = in.readInt();
                TreeMap<String, Object> m = new TreeMap<>();
                for (int j = 0; j < keyCount; j++) {
                    String key = readString(in);
                    m.put(key, readValue(in));
                }
                if (!m.isEmpty()) {
                    out.put(scope, m);
                }
            }
        } catch (EOFException ignored) {
        } catch (Exception ignored) {
        }
        return out;
    }

    private static void writeString(DataOutputStream out, String s) throws java.io.IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(b.length);
        out.write(b);
    }

    private static String readString(DataInputStream in) throws java.io.IOException {
        int len = in.readInt();
        byte[] b = new byte[len];
        in.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    private static void writeValue(DataOutputStream out, Object v) throws java.io.IOException {
        if (v instanceof Boolean b) {
            out.writeByte(T_BOOL);
            out.writeBoolean(b);
        } else if (v instanceof Integer i) {
            out.writeByte(T_INT);
            out.writeInt(i);
        } else if (v instanceof Long l) {
            out.writeByte(T_LONG);
            out.writeLong(l);
        } else if (v instanceof BigDecimal d) {
            out.writeByte(T_DEC);
            writeString(out, d.toPlainString());
        } else {
            out.writeByte(T_STRING);
            writeString(out, String.valueOf(v));
        }
    }

    private static Object readValue(DataInputStream in) throws java.io.IOException {
        byte tag = in.readByte();
        return switch (tag) {
            case T_BOOL -> in.readBoolean();
            case T_INT -> in.readInt();
            case T_LONG -> in.readLong();
            case T_DEC -> new BigDecimal(readString(in));
            default -> readString(in);
        };
    }

    private static final class Delta {
        final String scope;
        final String key;
        final Object value;

        private Delta(String scope, String key, Object value) {
            this.scope = scope;
            this.key = key;
            this.value = value;
        }

        static Delta set(String scope, String key, Object value) {
            return new Delta(scope, key, value);
        }

        static Delta delete(String scope, String key) {
            return new Delta(scope, key, null);
        }
    }
}

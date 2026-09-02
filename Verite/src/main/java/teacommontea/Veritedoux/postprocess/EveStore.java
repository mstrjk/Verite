package teacommontea.veritedoux.postprocess;

import org.bukkit.plugin.Plugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import teacommontea.veritedoux.EveEntry;
import teacommontea.util.DaemonExecutor;

public final class EveStore {

    private static final int FLAGS_MAGIC  = 0x464C4731;
    private static final String COLUMN = "flags";

    private final Map<UUID, Integer> flagCount = new HashMap<>();
    private final java.io.ByteArrayOutputStream h2Buffer = new java.io.ByteArrayOutputStream();

    private final java.util.concurrent.ExecutorService io = DaemonExecutor.single("Eve-IO");

    private EveStore() {
    }

    public static EveStore open(Plugin plugin) throws Exception {
        EveStore s = new EveStore();
        byte[] existing = s.loadFlagCounts();
        if (existing != null) {
            s.h2Buffer.write(existing);
        }
        return s;
    }

    private byte[] loadFlagCounts() {
        teacommontea.util.VeriteH2 db = teacommontea.util.VeriteH2.active();
        if (db == null) {
            return null;
        }
        byte[] bytes = db.read(COLUMN);
        if (bytes == null) {
            return null;
        }
        try (DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(bytes))) {
            while (true) {
                long hi, lo;
                try {
                    hi = in.readLong();
                } catch (EOFException eof) {
                    break;
                }
                lo = in.readLong();
                in.readLong();
                skipString(in);
                skipString(in);
                UUID u = new UUID(hi, lo);
                flagCount.merge(u, 1, Integer::sum);
            }
        } catch (Exception ignored) {
        }
        return bytes;
    }

    public void record(UUID player, EveEntry.Result category, String message) {
        synchronized (flagCount) {
            flagCount.merge(player, 1, Integer::sum);
        }
        String cat = category.name();
        String msg = message.length() > 512 ? message.substring(0, 512) : message;
        long at = System.currentTimeMillis();
        io.execute(() -> {
            teacommontea.util.VeriteH2 db = teacommontea.util.VeriteH2.active();
            if (db == null) {
                return;
            }
            try {
                synchronized (h2Buffer) {
                    DataOutputStream out = new DataOutputStream(h2Buffer);
                    out.writeLong(player.getMostSignificantBits());
                    out.writeLong(player.getLeastSignificantBits());
                    out.writeLong(at);
                    writeString(out, cat);
                    writeString(out, msg);
                    db.write(COLUMN, h2Buffer.toByteArray());
                }
            } catch (Exception ignored) {
            }
        });
    }

    public int count(UUID player) {
        synchronized (flagCount) {
            return flagCount.getOrDefault(player, 0);
        }
    }

    private static void writeString(DataOutputStream out, String s) throws Exception {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(b.length, 0xFFFF);
        out.writeShort(len);
        out.write(b, 0, len);
    }

    private static void skipString(DataInputStream in) throws Exception {
        int len = in.readUnsignedShort();
        int skipped = 0;
        while (skipped < len) {
            long s = in.skip(len - skipped);
            if (s <= 0) { in.readByte(); s = 1; }
            skipped += s;
        }
    }

    public void close() {
        io.shutdown();
        try {
            io.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }
}

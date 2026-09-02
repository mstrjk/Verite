package teacommontea.veritedoux.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EveVlex9 implements Closeable {

    static final String[] REGISTERS = {"MT", "LY", "SP", "ID", "NA", "HI", "IN", "OP", "IP"};
    private static final int SP = 2, ID = 3, OP = 7, IP = 8;

    private static volatile boolean nativeLoaded;
    private static Throwable nativeError;

    public static void configureNativeDir(java.io.File dir) {
    }

    private static synchronized void ensureLoaded() throws IOException {
        if (nativeLoaded) {
            if (nativeError != null) throw new IOException("native VLEX9 library unavailable", nativeError);
            return;
        }
        nativeLoaded = true;
        try {
            loadNative();
        } catch (Throwable t) {
            nativeError = t;
            throw new IOException("native VLEX9 library unavailable", t);
        }
    }

    private static void loadNative() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();

        String osDir;
        String libName;
        if (os.contains("win")) {
            osDir = "windows";
            libName = "vlex9_reader.dll";
        } else {
            osDir = "linux";
            libName = "libvlex9_reader.so";
        }

        String archDir;
        if (arch.equals("amd64") || arch.equals("x86_64") || arch.equals("x64")) {
            archDir = "x86-64";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            archDir = "aarch64";
        } else {
            archDir = arch;
        }

        String resource = "/.native/" + osDir + "-" + archDir + "/" + libName;
        byte[] bytes;
        try (java.io.InputStream in = EveVlex9.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("native VLEX9 lib not found in jar at " + resource);
            }
            bytes = in.readAllBytes();
        }

        int dot = libName.lastIndexOf('.');
        java.io.File tmp = java.io.File.createTempFile("vlex9_reader", libName.substring(dot));
        tmp.deleteOnExit();
        java.nio.file.Files.write(tmp.toPath(), bytes);
        System.load(tmp.getAbsolutePath());
    }

    private long handle;

    public EveVlex9(Path path) throws IOException {
        ensureLoaded();
        if (!Files.isRegularFile(path)) throw new IOException("VLEX9 file not found: " + path);
        long h = nOpen(path.toAbsolutePath().toString());
        if (h == 0) {
            String reason = nLastError();
            throw new IOException(reason == null || reason.isEmpty() ? "VLEX9 open failed" : "VLEX9 open failed: " + reason);
        }
        this.handle = h;
    }

    public String[] languageNames() {
        return nLanguageNames(handle);
    }

    public long[] languageTotals() {
        return nLanguageTotals(handle);
    }

    public LookupResult lookup(byte[] key) throws IOException {
        if (handle == 0) throw new IOException("VLEX9 reader closed");
        long[] packed = nLookup(handle, key);
        if (packed == null) return null;
        List<Row> rows = new ArrayList<>();
        int i = 0;
        int rowCount = (int) packed[i++];
        for (int r = 0; r < rowCount; r++) {
            int languageId = (int) packed[i++];
            long count = packed[i++];
            long sentences = packed[i++];
            int[] registers = new int[9];
            for (int j = 0; j < 9; j++) registers[j] = (int) packed[i++];
            rows.add(new Row(languageId, count, sentences, registers));
        }
        return new LookupResult(key, rows);
    }

    @Override
    public void close() {
        if (handle != 0) {
            nClose(handle);
            handle = 0;
        }
    }

    public static int score(int[] r) { return r[IP] + r[OP] - r[ID] - r[SP]; }

    public static final class LookupResult {
        public final byte[] word;
        public final Row[] rows;
        LookupResult(byte[] word, List<Row> rows) {
            this.word = word.clone();
            this.rows = rows.toArray(Row[]::new);
        }
    }

    public record Row(int languageId, long count, long sentences, int[] registers) {
        public Row {
            if (languageId < 0 || languageId >= 15) throw new IllegalArgumentException("language id outside 0..14");
            if (count < 0 || sentences < 0) throw new IllegalArgumentException("negative exact field");
            if (registers.length != 9) throw new IllegalArgumentException();
            registers = registers.clone();
            for (int x : registers) if (x < 0 || x > 1000) throw new IllegalArgumentException("register outside 0..1000");
        }
        @Override public int[] registers() { return registers; }
    }

    private static native long nOpen(String path);
    private static native String nLastError();
    private static native String[] nLanguageNames(long handle);
    private static native long[] nLanguageTotals(long handle);
    private static native long[] nLookup(long handle, byte[] key);
    private static native void nClose(long handle);
}

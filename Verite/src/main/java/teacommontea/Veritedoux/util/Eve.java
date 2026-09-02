package teacommontea.veritedoux.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class Eve implements AutoCloseable {

    private static boolean NATIVE_OK;
    private static Throwable NATIVE_ERR;

    private static final String CAP_PAIR = "";
    private static final String CAP_KV = "";

    private static volatile java.io.File nativeDir;
    private static volatile boolean nativeLoaded;

    public static void configureNativeDir(java.io.File dir) {
        nativeDir = dir;
    }

    public static java.io.File nativeDir() {
        return nativeDir;
    }

    private static synchronized void ensureLoaded() {
        if (nativeLoaded) {
            return;
        }
        nativeLoaded = true;
        try {
            loadNative();
            NATIVE_OK = true;
        } catch (Throwable t) {
            NATIVE_OK = false;
            NATIVE_ERR = t;
        }
    }

    public static boolean nativeAvailable() {
        ensureLoaded();
        return NATIVE_OK;
    }

    public static Throwable nativeError() {
        ensureLoaded();
        return NATIVE_ERR;
    }

    private static void loadNative() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();

        String osDir;
        String libName;
        if (os.contains("win")) {
            osDir = "windows";
            libName = "eve.dll";
        } else if (os.contains("mac") || os.contains("darwin")) {
            osDir = "macos";
            libName = "libeve.dylib";
        } else {
            osDir = isMusl() ? "linux-musl" : "linux";
            libName = "libeve.so";
        }

        String archDir;
        if (arch.equals("amd64") || arch.equals("x86_64") || arch.equals("x64")) {
            archDir = "x86-64";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            archDir = "aarch64";
        } else {
            archDir = arch;
        }

        java.io.File dir = nativeDir;
        if (dir == null) {
            throw new IOException("native directory not configured; call Eve.configureNativeDir first");
        }
        java.io.File lib = new java.io.File(dir, (osDir + "-" + archDir) + java.io.File.separator + libName);
        if (!lib.isFile()) {
            throw new IOException("no native lib on disk at " + lib.getAbsolutePath());
        }
        System.load(lib.getAbsolutePath());
    }

    private static boolean isMusl() {
        try {
            Path p = Path.of("/proc/self/maps");
            if (Files.exists(p)) {
                String maps = Files.readString(p);
                return maps.contains("musl") || maps.contains("ld-musl");
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private long handle;

    private Eve(long handle) {
        this.handle = handle;
    }

    public static Eve parse(String source) {
        long h = nParse(source);
        if (h == 0) {
            String reason = nLastError();
            throw new IllegalArgumentException(
                    reason == null || reason.isEmpty() ? "EVE parse failed" : "EVE parse failed: " + reason);
        }
        return new Eve(h);
    }

    public int ruleCount() {
        return nRuleCount(handle);
    }

    public String ruleName(int i) {
        return nRuleName(handle, i);
    }

    public String ruleRealm(int i) {
        return nRuleRealm(handle, i);
    }

    public boolean matches(int rule, String candidate) {
        return nMatches(handle, rule, candidate, candidate);
    }

    public boolean matches(int rule, String candidate, String fullLine) {
        return nMatches(handle, rule, candidate, fullLine);
    }

    public int firstMatch(String candidate, String fullLine) {
        return nFirstMatch(handle, candidate, fullLine);
    }

    public List<Match> scan(String candidate) {
        return scan(candidate, candidate);
    }

    public String gateKey(String fullLine) {
        return nGateKey(handle, fullLine);
    }

    public List<Match> scan(String candidate, String fullLine) {
        String packed = nScan(handle, candidate, fullLine);
        List<Match> out = new ArrayList<>();
        if (packed == null || packed.isEmpty()) {
            return out;
        }
        for (String row : packed.split("\n")) {
            String[] col = row.split("\t", -1);
            if (col.length < 4) continue;
            int rule = Integer.parseInt(col[0]);
            java.util.Map<String, Boolean> flags = new java.util.LinkedHashMap<>();
            if (!col[3].isEmpty()) {
                for (String f : col[3].split(" ")) {
                    int eq = f.lastIndexOf('=');
                    if (eq >= 0) flags.put(f.substring(0, eq), f.charAt(eq + 1) == '1');
                }
            }
            java.util.Map<String, String> caps = new java.util.LinkedHashMap<>();
            if (col.length >= 5 && !col[4].isEmpty()) {
                for (String pair : col[4].split(CAP_PAIR)) {
                    int sep = pair.indexOf(CAP_KV);
                    if (sep >= 0) caps.put(pair.substring(0, sep), pair.substring(sep + CAP_KV.length()));
                }
            }
            out.add(new Match(rule, col[1], col[2], flags, caps));
        }
        return out;
    }

    public record Match(int rule, String name, String realm,
                        java.util.Map<String, Boolean> flags,
                        java.util.Map<String, String> captures) {
        public boolean flag(String name, boolean dflt) {
            return flags.getOrDefault(name, dflt);
        }
        public String capture(String name) {
            return captures.get(name);
        }
    }

    public int ruleKind(int i) {
        return nRuleKind(handle, i);
    }

    public int anywhereGroupCount(int rule) {
        return nAnywhereGroupCount(handle, rule);
    }

    public List<AnywhereHit> anywhereHits(String candidate, String fullLine) {
        String packed = nAnywhereHits(handle, candidate, fullLine);
        List<AnywhereHit> out = new ArrayList<>();
        if (packed == null || packed.isEmpty()) {
            return out;
        }
        for (String row : packed.split("\n")) {
            String[] col = row.split("\t", -1);
            if (col.length < 4) continue;
            int rule = Integer.parseInt(col[0]);
            int[] groups;
            if (col[3].isEmpty()) {
                groups = new int[0];
            } else {
                String[] gs = col[3].split(",");
                groups = new int[gs.length];
                for (int i = 0; i < gs.length; i++) {
                    groups[i] = Integer.parseInt(gs[i]);
                }
            }
            out.add(new AnywhereHit(rule, col[1], col[2], groups));
        }
        return out;
    }

    public record AnywhereHit(int rule, String name, String realm, int[] groups) {}

    public List<Match> sequenceHits(String message) {
        String packed = nSequenceHits(handle, message);
        List<Match> out = new ArrayList<>();
        if (packed == null || packed.isEmpty()) {
            return out;
        }
        for (String row : packed.split("\n")) {
            String[] col = row.split("\t", -1);
            if (col.length < 3) continue;
            int rule = Integer.parseInt(col[0]);
            out.add(new Match(rule, col[1], col[2],
                    new java.util.LinkedHashMap<>(), new java.util.LinkedHashMap<>()));
        }
        return out;
    }

    public List<Flag> flags(int rule) {
        String packed = nFlags(handle, rule);
        List<Flag> out = new ArrayList<>();
        if (packed == null || packed.isEmpty()) {
            return out;
        }
        for (String f : packed.split(" ")) {
            int eq = f.lastIndexOf('=');
            if (eq < 0) continue;
            out.add(new Flag(f.substring(0, eq), f.charAt(eq + 1) == '1'));
        }
        return out;
    }

    public record Flag(String name, boolean value) {}

    public record ConcreteWord(String word, java.util.Map<String, Boolean> flags) {
        public boolean flag(String name, boolean dflt) {
            return flags.getOrDefault(name, dflt);
        }
    }

    public List<String> ruleAnchors(int rule) {
        String packed = nRuleAnchors(handle, rule);
        List<String> out = new ArrayList<>();
        if (packed == null || packed.isEmpty()) {
            return out;
        }
        for (String s : packed.split("\n")) {
            out.add(s);
        }
        return out;
    }

    public String anchorReason(int rule) {
        String s = nAnchorReason(handle, rule);
        return s == null ? "" : s;
    }

    public List<ConcreteWord> concreteWords() {
        String packed = nConcreteWords(handle);
        List<ConcreteWord> out = new ArrayList<>();
        if (packed == null || packed.isEmpty()) {
            return out;
        }
        for (String row : packed.split("\n")) {
            int tab = row.indexOf('\t');
            if (tab < 0) continue;
            String word = row.substring(0, tab);
            java.util.Map<String, Boolean> flags = new java.util.LinkedHashMap<>();
            String fstr = row.substring(tab + 1);
            if (!fstr.isEmpty()) {
                for (String f : fstr.split(" ")) {
                    int eq = f.lastIndexOf('=');
                    if (eq >= 0) flags.put(f.substring(0, eq), f.charAt(eq + 1) == '1');
                }
            }
            out.add(new ConcreteWord(word, flags));
        }
        return out;
    }

    @Override
    public void close() {
        if (handle != 0) {
            nFree(handle);
            handle = 0;
        }
    }

    public static boolean loadFold(String confusablesText) {
        if (!nativeAvailable()) {
            return false;
        }
        return nLoadFold(confusablesText == null ? "" : confusablesText);
    }

    public static List<String> candidates(String message, boolean entityStrip, boolean homoglyphFold, int maxCandidates) {
        List<String> out = new ArrayList<>();
        if (message == null) {
            return out;
        }
        if (!nativeAvailable()) {
            out.add(message);
            return out;
        }
        String packed = nCandidates(message, entityStrip, homoglyphFold, maxCandidates);
        if (packed == null || packed.isEmpty()) {
            out.add(message);
            return out;
        }
        for (String s : packed.split("\n", -1)) {
            out.add(s);
        }
        return out;
    }

    public static String reduceRuns(String s, int maxRun) {
        if (s == null || !nativeAvailable()) {
            return s;
        }
        return nReduceRuns(s, maxRun);
    }

    public static String stripEntities(String s) {
        if (s == null || !nativeAvailable()) {
            return s;
        }
        return nStripEntities(s);
    }

    public static String stripDeletes(String s) {
        if (s == null || !nativeAvailable()) {
            return s;
        }
        return nStripDeletes(s);
    }

    public static String fingerprint(String w) {
        if (w == null || !nativeAvailable()) {
            return w;
        }
        return nFingerprint(w);
    }

    public static String denseStrip(String token) {
        if (token == null || !nativeAvailable()) {
            return null;
        }
        String r = nDenseStrip(token);
        return r == null || r.isEmpty() ? null : r;
    }

    private static native long nParse(String source);
    private static native String nLastError();
    private static native boolean nLoadFold(String text);
    private static native String nCandidates(String message, boolean entityStrip, boolean homoglyphFold, int maxCandidates);
    private static native String nReduceRuns(String s, int maxRun);
    private static native String nStripEntities(String s);
    private static native String nStripDeletes(String s);
    private static native String nFingerprint(String w);
    private static native String nDenseStrip(String token);
    private static native void nFree(long handle);
    private static native int nRuleCount(long handle);
    private static native String nRuleName(long handle, int i);
    private static native String nRuleRealm(long handle, int i);
    private static native int nRuleKind(long handle, int i);
    private static native int nAnywhereGroupCount(long handle, int i);
    private static native String nAnywhereHits(long handle, String candidate, String fullLine);
    private static native String nSequenceHits(long handle, String message);
    private static native boolean nMatches(long handle, int rule, String candidate, String fullLine);
    private static native int nFirstMatch(long handle, String candidate, String fullLine);
    private static native String nGateKey(long handle, String fullLine);
    private static native String nScan(long handle, String candidate, String fullLine);
    private static native String nFlags(long handle, int rule);
    private static native String nConcreteWords(long handle);
    private static native String nRuleAnchors(long handle, int rule);
    private static native String nAnchorReason(long handle, int rule);
}

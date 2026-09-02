package teacommontea.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SelfHeal {

    private SelfHeal() {}

    public static void healSettings(Plugin plugin) {
        if (!updatesEnabled(plugin)) return;
        try {
            doHealSettings(plugin);
        } catch (Exception e) {
            plugin.getLogger().warning("settings.yml self-heal skipped: " + e.getMessage());
        }
    }

    public static void migrate(Plugin plugin) {
        if (!updatesEnabled(plugin)) return;
        try {
            doMigrate(plugin);
        } catch (Exception e) {
            plugin.getLogger().warning("config migration skipped: " + e.getMessage());
        }
    }

    public static void validate(Plugin plugin) {
        try {
            File deployed = new File(plugin.getDataFolder(), "config.yml");
            if (!deployed.isFile()) return;
            String bundledText = readResource(plugin, "config.yml");
            if (bundledText == null) return;
            String deployedText = new String(Files.readAllBytes(deployed.toPath()), StandardCharsets.UTF_8);

            String reason = malformedReason(bundledText, deployedText);
            if (reason == null) return;

            Files.write(deployed.toPath(), bundledText.getBytes(StandardCharsets.UTF_8));
            plugin.getLogger().warning("config.yml was malformed (" + reason
                    + "); it has been reset to the default. Any custom edits were discarded.");
        } catch (Exception e) {
            plugin.getLogger().warning("config validation skipped: " + e.getMessage());
        }
    }

    static String malformedReason(String bundledText, String deployedText) {
        try {
            new org.bukkit.configuration.file.YamlConfiguration()
                    .loadFromString(teacommontea.util.Yaml.protectForValidation(deployedText));
        } catch (Throwable t) {
            return "invalid YAML";
        }

        List<String> deployedLines = Yaml.splitLines(deployedText);
        List<Entry> deployedEntries = parse(deployedLines);

        Set<String> seen = new java.util.HashSet<>();
        for (Entry e : deployedEntries) {
            if (!seen.add(e.path)) {
                return "duplicate key " + e.path.replace(SEP, '.');
            }
        }

        Set<String> allowed = paths(parse(Yaml.splitLines(bundledText)));
        for (Entry e : deployedEntries) {
            if (!allowed.contains(e.path)) {
                return "unknown key " + e.path.replace(SEP, '.');
            }
        }
        return null;
    }

    private static void doMigrate(Plugin plugin) throws Exception {
        String manifest = readResource(plugin, "config_migrations.txt");
        if (manifest == null) return;

        java.util.Map<String, List<String>> buffers = new java.util.LinkedHashMap<>();
        java.util.Map<String, Boolean> dirty = new java.util.LinkedHashMap<>();
        java.util.Set<String> deletedFiles = new java.util.LinkedHashSet<>();

        List<String> manifestLines = new ArrayList<>(Yaml.splitLines(manifest));
        java.util.Collections.reverse(manifestLines);
        for (String raw : manifestLines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] tok = line.split("\\s+");
            String verb = tok[0].toUpperCase(java.util.Locale.ROOT);

            if (verb.equals("MIGRATE") && tok.length >= 5
                    && tok[2].equalsIgnoreCase("COLUMN") && tok[4].equalsIgnoreCase("OF")) {
                if (migrateToColumn(plugin, tok[1], tok[3])) {
                    deletedFiles.add(tok[1]);
                }
                continue;
            }

            if (verb.equals("DROP") && tok.length >= 4
                    && tok[1].equalsIgnoreCase("H2") && tok[2].equalsIgnoreCase("COLUMN")) {
                if (VeriteH2.isActive() && VeriteH2.active().remove(tok[3])) {
                    plugin.getLogger().info("config migration: dropped H2 column '"
                            + tok[3] + "'.");
                }
                continue;
            }

            if (verb.equals("DELETE") && tok.length >= 2) {
                if (guardedByH2(tok) && !VeriteH2.isActive()) {
                    continue;
                }
                File f = resolveUnder(plugin.getDataFolder(), plugin.getDataFolder(), tok[1]);
                if (f != null && f.exists() && !f.equals(plugin.getDataFolder())
                        && deleteRecursively(f)) {
                    deletedFiles.add(tok[1]);
                }
                continue;
            }

            if (verb.equals("MOVE") && tok.length >= 3) {
                if (moveEntry(plugin, tok[1], tok[2])) {
                    deletedFiles.add(tok[1]);
                }
                continue;
            }

            if (verb.equals("RENAME") && tok.length >= 4 && tok[1].equalsIgnoreCase("DIR")) {
                if (renameDir(plugin, tok[2], tok[3])) {
                    deletedFiles.add(tok[2]);
                }
                continue;
            }

            if (verb.equals("RENAME") && tok.length >= 3) {
                String file = fromFile(tok, 3);
                List<String> lines = buffer(plugin, buffers, file);
                if (lines != null && renameKey(lines, tok[1], tok[2])) {
                    dirty.put(file, true);
                }
                continue;
            }

            if (verb.equals("DROP")) {
                boolean parent = tok.length >= 2 && tok[1].equalsIgnoreCase("PARENT");
                int pathIdx = parent ? 2 : 1;
                if (tok.length <= pathIdx) continue;
                String path = tok[pathIdx];
                String file = fromFile(tok, pathIdx + 1);
                List<String> lines = buffer(plugin, buffers, file);
                if (lines != null && dropKey(lines, path, parent)) {
                    dirty.put(file, true);
                }
            }
        }

        int changed = 0;
        for (java.util.Map.Entry<String, List<String>> e : buffers.entrySet()) {
            if (!Boolean.TRUE.equals(dirty.get(e.getKey()))) continue;
            File f = new File(plugin.getDataFolder(), e.getKey());
            Files.write(f.toPath(), (String.join("\n", e.getValue()) + "\n").getBytes(StandardCharsets.UTF_8));
            changed++;
        }
        if (changed > 0 || !deletedFiles.isEmpty()) {
            plugin.getLogger().info("config migration: " + deletedFiles.size()
                    + " legacy file(s) removed, " + changed + " file(s) rewritten.");
        }
    }

    private static boolean deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) {
                for (File k : kids) {
                    deleteRecursively(k);
                }
            }
        }
        return f.delete();
    }

    private static boolean guardedByH2(String[] tok) {
        for (int i = 2; i + 2 < tok.length; i++) {
            if (tok[i].equalsIgnoreCase("IF") && tok[i + 1].equalsIgnoreCase("H2")
                    && tok[i + 2].equalsIgnoreCase("EXISTS")) {
                return true;
            }
        }
        return false;
    }

    private static boolean migrateToColumn(Plugin plugin, String fileName, String column) {
        if (!VeriteH2.isActive()) {
            return false;
        }
        VeriteH2 db = VeriteH2.active();
        if (db.read(column) != null) {
            return false;
        }
        File src = new File(plugin.getDataFolder(), fileName);
        if (!src.isFile()) {
            return false;
        }
        try {
            byte[] bytes = Files.readAllBytes(src.toPath());
            db.write(column, bytes);
            plugin.getLogger().info("migrated " + fileName + " into H2 column '" + column + "'.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("could not migrate " + fileName
                    + " into H2 column '" + column + "': " + e.getMessage());
            return false;
        }
    }

    private static boolean renameDir(Plugin plugin, String src, String dst) {
        File base = plugin.getDataFolder();
        File from = resolveUnder(base, base, src);
        File to = resolveUnder(base, base, dst);
        if (from == null || to == null || !from.exists() || from.equals(to) || to.exists()) {
            return false;
        }
        try {
            File parent = to.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.move(from.toPath(), to.toPath());
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("config migration: could not rename directory " + src
                    + " to " + dst + ": " + e.getMessage());
            return false;
        }
    }

    private static boolean moveEntry(Plugin plugin, String src, String dst) {
        File base = plugin.getDataFolder();
        File from = resolveUnder(base, base, src);
        if (from == null || !from.exists()) {
            return false;
        }
        File to = resolveUnder(base, from.getParentFile(), dst);
        if (to == null) {
            return false;
        }
        if (to.isDirectory()) {
            to = new File(to, from.getName());
        }
        if (from.equals(to)) {
            return false;
        }
        try {
            File parent = to.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (to.exists()) {
                return false;
            }
            Files.move(from.toPath(), to.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (Exception atomicFailed) {
            try {
                Files.move(from.toPath(), to.toPath());
                return true;
            } catch (Exception e) {
                plugin.getLogger().warning("config migration: could not move " + src
                        + " to " + dst + ": " + e.getMessage());
                return false;
            }
        }
    }

    private static File resolveUnder(File rootDir, File anchorDir, String path) {
        String norm = path.replace('\\', '/');
        java.nio.file.Path root = rootDir.toPath().normalize();
        java.nio.file.Path anchor;
        if (norm.startsWith("/")) {
            while (norm.startsWith("/")) {
                norm = norm.substring(1);
            }
            anchor = root;
        } else {
            anchor = anchorDir.toPath().normalize();
        }
        java.nio.file.Path resolved = anchor.resolve(norm).normalize();
        if (!resolved.startsWith(root)) {
            return null;
        }
        return resolved.toFile();
    }

    private static String fromFile(String[] tok, int i) {
        if (i + 1 < tok.length && tok[i].equalsIgnoreCase("FROM")) {
            return tok[i + 1];
        }
        return "config.yml";
    }

    private static List<String> buffer(Plugin plugin, java.util.Map<String, List<String>> buffers, String file) {
        if (buffers.containsKey(file)) {
            return buffers.get(file);
        }
        File f = new File(plugin.getDataFolder(), file);
        List<String> lines = null;
        if (f.isFile()) {
            try {
                lines = new ArrayList<>(Yaml.splitLines(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)));
            } catch (Exception ignored) {
            }
        }
        buffers.put(file, lines);
        return lines;
    }

    private static boolean renameKey(List<String> lines, String oldPath, String newPath) {
        List<Entry> entries = parse(lines);
        Entry e = find(entries, dslPath(entries, oldPath));
        if (e == null) return false;
        int lastDot = oldPath.lastIndexOf('.');
        String parent = lastDot < 0 ? null : oldPath.substring(0, lastDot);
        String newLeaf = parent != null && newPath.startsWith(parent + ".")
                ? newPath.substring(parent.length() + 1)
                : newPath.substring(newPath.lastIndexOf('.') + 1);
        String line = lines.get(e.keyLine);
        int colon = Yaml.keyColon(line);
        if (colon < 0) return false;
        String indent = line.substring(0, e.indent);
        lines.set(e.keyLine, indent + newLeaf + line.substring(colon));
        return true;
    }

    private static boolean dropKey(List<String> lines, String path, boolean parent) {
        List<Entry> entries = parse(lines);
        Entry e = find(entries, dslPath(entries, path));
        if (e == null) return false;
        int end = parent ? blockEnd(lines, e) : singleKeyEnd(lines, e);
        for (int j = end - 1; j >= e.commentStart; j--) {
            lines.remove(j);
        }
        return true;
    }

    private static int singleKeyEnd(List<String> lines, Entry e) {
        int j = e.keyLine + 1;
        while (j < lines.size()) {
            String t = lines.get(j).trim();
            if (t.isEmpty()) break;
            if (Yaml.indentOf(lines.get(j)) <= e.indent && !t.startsWith("-")) break;
            j++;
        }
        return j;
    }

    private static boolean updatesEnabled(Plugin plugin) {
        try {
            File deployed = new File(plugin.getDataFolder(), "config.yml");
            if (!deployed.isFile()) return true;
            for (String line : Yaml.splitLines(new String(Files.readAllBytes(deployed.toPath()),
                    StandardCharsets.UTF_8))) {
                String t = line.trim();
                if (t.startsWith("#")) continue;
                int colon = t.indexOf(':');
                if (colon < 0) continue;
                if (!t.substring(0, colon).trim().equals("auto.update.config")) continue;
                String v = t.substring(colon + 1).trim().toLowerCase(java.util.Locale.ROOT);
                return !(v.equals("false") || v.equals("no") || v.equals("off"));
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private static final String[] SETTINGS_FILES = {"config.yml"};

    private static void doHealSettings(Plugin plugin) throws Exception {
        for (String name : SETTINGS_FILES) {
            File deployed = new File(plugin.getDataFolder(), name);
            if (!deployed.isFile()) continue;

            String bundledText = readResource(plugin, name);
            if (bundledText == null) continue;

            String deployedText = new String(Files.readAllBytes(deployed.toPath()), StandardCharsets.UTF_8);

            String baseText = readConfigBase(name);
            String rebuilt = baseText == null
                    ? rebuildFromBundled(bundledText, deployedText)
                    : rebuildFromBundled(bundledText, deployedText, baseText);

            writeConfigBase(name, bundledText);

            if (rebuilt.equals(deployedText)) continue;

            Files.write(deployed.toPath(), rebuilt.getBytes(StandardCharsets.UTF_8));
            plugin.getLogger().info("" + name
                    + " brought up to date with the current config structure; owner option values were preserved.");
        }
    }

    private static String readConfigBase(String file) {
        VeriteH2 h2 = VeriteH2.active();
        if (h2 == null) {
            return null;
        }
        byte[] stored = h2.read("config-base:" + file);
        return stored == null ? null : new String(stored, StandardCharsets.UTF_8);
    }

    private static void writeConfigBase(String file, String bundledText) {
        VeriteH2 h2 = VeriteH2.active();
        if (h2 != null) {
            h2.write("config-base:" + file, bundledText.getBytes(StandardCharsets.UTF_8));
        }
    }

    static String rebuildFromBundled(String bundledText, String deployedText) {
        return rebuildFromBundled(bundledText, deployedText, null);
    }

    static String rebuildFromBundled(String bundledText, String deployedText, String baseText) {
        List<String> bundledLines = Yaml.splitLines(bundledText);
        List<String> deployedLines = Yaml.splitLines(deployedText);
        List<Entry> bundledEntries = parse(bundledLines);
        List<Entry> deployedEntries = parse(deployedLines);

        java.util.Map<String, String> deployedValues = new java.util.HashMap<>();
        for (Entry e : deployedEntries) {
            String v = scalarValue(deployedLines.get(e.keyLine));
            if (v != null) deployedValues.put(e.path, v);
        }

        java.util.Map<String, String> baseValues = new java.util.HashMap<>();
        if (baseText != null) {
            List<String> baseLines = Yaml.splitLines(baseText);
            for (Entry e : parse(baseLines)) {
                String v = scalarValue(baseLines.get(e.keyLine));
                if (v != null) baseValues.put(e.path, v);
            }
        }

        java.util.Map<Integer, Entry> bundledByLine = new java.util.HashMap<>();
        for (Entry e : bundledEntries) bundledByLine.put(e.keyLine, e);

        List<String> out = new ArrayList<>(bundledLines);
        for (int i = 0; i < out.size(); i++) {
            Entry e = bundledByLine.get(i);
            if (e == null) continue;
            String bundledVal = scalarValue(out.get(i));
            if (bundledVal == null) continue;
            String ownerVal = deployedValues.get(e.path);
            if (ownerVal == null) continue;
            if (!sameShape(bundledVal, ownerVal)) continue;
            if (ownerVal.equals(bundledVal)) continue;
            if (baseText != null) {
                String baseVal = baseValues.get(e.path);
                if (baseVal != null && baseVal.equals(ownerVal)) continue;
            }
            out.set(i, replaceScalarValue(out.get(i), ownerVal));
        }

        String eol = bundledText.contains("\r\n") ? "\r\n" : "\n";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < out.size(); i++) {
            sb.append(out.get(i));
            if (i < out.size() - 1) sb.append(eol);
        }
        if (bundledText.endsWith("\n") || bundledText.endsWith("\r")) sb.append(eol);
        return sb.toString();
    }

    static String scalarValue(String line) {
        int colon = Yaml.keyColon(line);
        if (colon < 0) return null;
        String after = line.substring(colon + 1);
        int hash = Yaml.commentStartOutsideQuotes(after);
        if (hash >= 0) after = after.substring(0, hash);
        String v = after.trim();
        if (v.isEmpty()) return null;
        return v;
    }

    private static String replaceScalarValue(String line, String newValue) {
        int colon = Yaml.keyColon(line);
        String after = line.substring(colon + 1);
        int hash = Yaml.commentStartOutsideQuotes(after);
        String trailing = hash >= 0 ? after.substring(hash) : "";
        int lead = 0;
        while (lead < after.length() && after.charAt(lead) == ' ') lead++;
        String leadSpace = after.substring(0, Math.max(1, lead));
        String trailSpace = trailing.isEmpty() ? "" : " ";
        return line.substring(0, colon + 1) + leadSpace + newValue + trailSpace + trailing;
    }

    static boolean sameShape(String bundled, String owner) {
        return valueShape(bundled) == valueShape(owner);
    }

    private static int valueShape(String v) {
        String t = v.trim();
        if (t.isEmpty()) return 0;
        if (t.startsWith("{") && t.endsWith("}")) return 1;
        if (t.startsWith("[") && t.endsWith("]")) return 2;
        String low = t.toLowerCase(java.util.Locale.ROOT);
        if (low.equals("true") || low.equals("false") || low.equals("yes")
                || low.equals("no") || low.equals("on") || low.equals("off")) return 3;
        if (isNumber(t)) return 4;
        if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) return 5;
        return 6;
    }

    private static boolean isNumber(String t) {
        boolean dot = false, digit = false;
        int i = 0;
        if (i < t.length() && (t.charAt(i) == '-' || t.charAt(i) == '+')) i++;
        for (; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '.') { if (dot) return false; dot = true; }
            else if (c >= '0' && c <= '9') digit = true;
            else return false;
        }
        return digit;
    }

    private static final char SEP = '\u0001';

    private static final class Entry {
        final String path;
        final String name;
        final int indent;
        final int keyLine;
        final int commentStart;
        Entry(String path, String name, int indent, int keyLine, int commentStart) {
            this.path = path; this.name = name; this.indent = indent;
            this.keyLine = keyLine; this.commentStart = commentStart;
        }
        String pathName() {
            return name;
        }
    }

    private static List<Entry> parse(List<String> lines) {
        List<Entry> entries = new ArrayList<>();
        List<int[]> stack = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            if (trimmed.startsWith("-")) continue;

            int colon = Yaml.keyColon(line);
            if (colon < 0) continue;

            int indent = Yaml.indentOf(line);
            String name = line.substring(indent, colon).trim();
            if (name.isEmpty()) continue;

            while (!stack.isEmpty() && stack.get(stack.size() - 1)[0] >= indent) {
                stack.remove(stack.size() - 1);
            }
            StringBuilder pathB = new StringBuilder();
            for (int[] anc : stack) {
                pathB.append(entries.get(anc[1]).pathName()).append(SEP);
            }
            pathB.append(name);
            String path = pathB.toString();

            int commentStart = commentStartFor(lines, i);
            Entry e = new Entry(path, name, indent, i, commentStart);
            entries.add(e);
            stack.add(new int[]{indent, entries.size() - 1});
        }
        return entries;
    }

    private static String dslPath(List<Entry> entries, String dotted) {
        for (Entry e : entries) {
            if (e.path.replace(SEP, '.').equals(dotted)) return e.path;
        }
        return dotted;
    }

    private static int commentStartFor(List<String> lines, int keyLine) {
        int start = keyLine;
        for (int j = keyLine - 1; j >= 0; j--) {
            String t = lines.get(j).trim();
            if (t.startsWith("#")) { start = j; continue; }
            break;
        }
        return start;
    }

    private static int blockEnd(List<String> lines, Entry e) {
        int end = e.keyLine + 1;
        int j = e.keyLine + 1;
        while (j < lines.size()) {
            String t = lines.get(j).trim();
            if (t.isEmpty() || t.startsWith("#")) {
                int k = j;
                while (k < lines.size()) {
                    String tk = lines.get(k).trim();
                    if (tk.isEmpty() || tk.startsWith("#")) { k++; continue; }
                    break;
                }
                if (k >= lines.size() || Yaml.indentOf(lines.get(k)) <= e.indent) break;
                j = k;
                continue;
            }
            if (Yaml.indentOf(lines.get(j)) <= e.indent) break;
            end = j + 1;
            j++;
        }
        return end;
    }

    private static Entry find(List<Entry> entries, String path) {
        for (Entry e : entries) if (e.path.equals(path)) return e;
        return null;
    }

    private static Set<String> paths(List<Entry> entries) {
        Set<String> s = new LinkedHashSet<>();
        for (Entry e : entries) s.add(e.path);
        return s;
    }

    public static void healEve(Plugin plugin, String[] eveConfigs) {
        if (!updatesEnabled(plugin)) return;
        try {
            doHealEve(plugin, eveConfigs);
        } catch (Exception e) {
            plugin.getLogger().warning("config .eve self-heal skipped: " + e.getMessage());
        }
    }

    public static void overwriteEve(Plugin plugin, String[] eveConfigs) {
        if (!updatesEnabled(plugin)) return;
        if (!eveOverwriteEnabled(plugin)) return;
        try {
            doOverwriteEve(plugin, eveConfigs);
        } catch (Exception e) {
            plugin.getLogger().warning("config .eve overwrite skipped: " + e.getMessage());
        }
    }

    public static void reconcileEve(Plugin plugin, String[] eveConfigs) {
        try {
            doReconcileEve(plugin, eveConfigs);
        } catch (Exception e) {
            plugin.getLogger().warning(".eve reconcile skipped: " + e.getMessage());
        }
    }

    private static void doReconcileEve(Plugin plugin, String[] eveConfigs) throws Exception {
        int preserved = 0;
        for (String config : eveConfigs) {
            File deployed = new File(plugin.getDataFolder(), config);
            File backup = new File(plugin.getDataFolder(), config + ".bak");
            if (!backup.isFile()) continue;
            try {
                if (!deployed.isFile()) {
                    continue;
                }
                String newText = new String(Files.readAllBytes(deployed.toPath()), StandardCharsets.UTF_8);
                String oldText = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);

                Set<String> newKeys = new LinkedHashSet<>();
                for (EveBlock b : eveBlocks(Yaml.splitLines(newText))) {
                    if (!b.isRealm) newKeys.add(ruleIdentity(b));
                }
                List<EveBlock> ownerOnly = new ArrayList<>();
                for (EveBlock b : eveBlocks(Yaml.splitLines(oldText))) {
                    if (b.isRealm) continue;
                    if (!newKeys.contains(ruleIdentity(b))) ownerOnly.add(b);
                }
                if (ownerOnly.isEmpty()) {
                    continue;
                }

                StringBuilder append = new StringBuilder();
                if (!newText.isEmpty() && !newText.endsWith("\n")) append.append('\n');
                append.append('\n');
                append.append("{#c: The rules below were carried over from your previous configuration ")
                        .append("because they were not part of this update. Review them and remove any you no ")
                        .append("longer want. }").append('\n');
                String lastRealm = null;
                for (EveBlock b : ownerOnly) {
                    if (b.realm != null && !b.realm.equals(lastRealm)) {
                        append.append('\n').append(b.realm).append('\n');
                        lastRealm = b.realm;
                    }
                    append.append('\n');
                    for (String line : b.lines) append.append(line).append('\n');
                }
                Files.write(deployed.toPath(), append.toString().getBytes(StandardCharsets.UTF_8),
                        java.nio.file.StandardOpenOption.APPEND);
                preserved += ownerOnly.size();
                plugin.getLogger().info("" + config + ": preserved " + ownerOnly.size()
                        + " edited rule" + (ownerOnly.size() == 1 ? "" : "s") + " across the update.");
            } finally {
                backup.delete();
            }
        }
        if (preserved > 0) {
            plugin.getLogger().info("filter update: carried over " + preserved
                    + " owner-edited rule" + (preserved == 1 ? "" : "s") + " in total.");
        }
    }

    public static boolean eveOverwriteEnabled(Plugin plugin) {
        try {
            File deployed = new File(plugin.getDataFolder(), "config.yml");
            if (!deployed.isFile()) return false;
            for (String line : Yaml.splitLines(new String(Files.readAllBytes(deployed.toPath()),
                    StandardCharsets.UTF_8))) {
                String t = line.trim();
                if (t.startsWith("#")) continue;
                int colon = t.indexOf(':');
                if (colon < 0) continue;
                if (!t.substring(0, colon).trim().equals("auto.update.eve")) continue;
                String v = t.substring(colon + 1).trim().toLowerCase(java.util.Locale.ROOT);
                return v.equals("true") || v.equals("yes") || v.equals("on");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static void doOverwriteEve(Plugin plugin, String[] eveConfigs) throws Exception {
        int rewritten = 0;
        for (String config : eveConfigs) {
            File deployed = new File(plugin.getDataFolder(), config);
            if (!deployed.isFile()) continue;

            String bundledText = readResource(plugin, config);
            if (bundledText == null) continue;

            byte[] bundledBytes = bundledText.getBytes(StandardCharsets.UTF_8);
            byte[] deployedBytes = Files.readAllBytes(deployed.toPath());
            if (java.util.Arrays.equals(bundledBytes, deployedBytes)) continue;

            Files.write(deployed.toPath(), bundledBytes);
            rewritten++;
        }
        if (rewritten > 0) {
            plugin.getLogger().info("auto.update.eve: overwrote " + rewritten
                    + " .eve file" + (rewritten == 1 ? "" : "s") + " with the current bundled config.");
        }
    }

    private static void doHealEve(Plugin plugin, String[] eveConfigs) throws Exception {
        for (String config : eveConfigs) {
            File deployed = new File(plugin.getDataFolder(), config);
            if (!deployed.isFile()) continue;

            String bundledText = readResource(plugin, config);
            if (bundledText == null) continue;

            String deployedText = new String(Files.readAllBytes(deployed.toPath()), StandardCharsets.UTF_8);

            List<EveBlock> bundled = eveBlocks(Yaml.splitLines(bundledText));
            Set<String> deployedKeys = new LinkedHashSet<>();
            for (EveBlock b : eveBlocks(Yaml.splitLines(deployedText))) deployedKeys.add(b.key);

            List<EveBlock> missing = new ArrayList<>();
            for (EveBlock b : bundled) {
                if (b.isRealm) continue;
                if (deployedKeys.contains(b.key)) continue;
                missing.add(b);
            }
            if (missing.isEmpty()) continue;

            StringBuilder append = new StringBuilder();
            if (!deployedText.isEmpty() && !deployedText.endsWith("\n")) append.append('\n');
            String lastRealm = null;
            Set<String> deployedRealms = new LinkedHashSet<>();
            for (EveBlock b : eveBlocks(Yaml.splitLines(deployedText))) {
                if (b.isRealm) deployedRealms.add(b.key);
            }
            for (EveBlock b : missing) {
                if (b.realm != null && !b.realm.equals(lastRealm) && !deployedRealms.contains(b.realm)) {
                    append.append('\n').append(b.realm).append('\n');
                    deployedRealms.add(b.realm);
                }
                lastRealm = b.realm;
                append.append('\n');
                for (String line : b.lines) append.append(line).append('\n');
            }

            Files.write(deployed.toPath(), append.toString().getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.APPEND);
            plugin.getLogger().info("" + config + " self-heal added " + missing.size()
                    + " new rule" + (missing.size() == 1 ? "" : "s") + " from the current config.");
        }
    }

    private static final class EveBlock {
        final List<String> lines;
        final String key;
        final boolean isRealm;
        final String realm;
        EveBlock(List<String> lines, String key, boolean isRealm, String realm) {
            this.lines = lines; this.key = key; this.isRealm = isRealm; this.realm = realm;
        }
    }

    private static List<EveBlock> eveBlocks(List<String> lines) {
        List<EveBlock> blocks = new ArrayList<>();
        String currentRealm = null;
        int i = 0;
        while (i < lines.size()) {
            String raw = lines.get(i);
            String t = raw.trim();
            if (t.isEmpty() || t.startsWith("#")) { i++; continue; }

            if (t.startsWith("REALM ") || t.equals("REALM")) {
                currentRealm = t;
                blocks.add(new EveBlock(List.of(raw), normalizeKey(List.of(t)), true, currentRealm));
                i++;
                continue;
            }

            List<String> block = new ArrayList<>();
            block.add(raw);
            int j = i + 1;
            while (j < lines.size()) {
                String next = lines.get(j);
                String nt = next.trim();
                if (nt.isEmpty()) break;
                if (startsStatement(nt)) break;
                block.add(next);
                j++;
            }
            blocks.add(new EveBlock(block, normalizeKey(block), false, currentRealm));
            i = j;
        }
        return blocks;
    }

    private static boolean startsStatement(String trimmed) {
        return trimmed.startsWith("HEAR ") || trimmed.equals("HEAR")
                || trimmed.startsWith("FIND ") || trimmed.equals("FIND")
                || trimmed.startsWith("MATCH ") || trimmed.equals("MATCH")
                || trimmed.startsWith("ANYWHERE ") || trimmed.equals("ANYWHERE")
                || trimmed.startsWith("REALM ") || trimmed.equals("REALM")
                || trimmed.startsWith("DEFINE ")
                || trimmed.startsWith("LET ");
    }

    private static final Set<String> EVE_RULE_MODIFIERS = Set.of(
            "SUFFIX", "PREFIX", "PLURAL", "NEVER", "SKELETON", "BEFORE", "AFTER",
            "FLAG", "GAP", "ALSO", "MAYBE", "SWAP", "LETTER", "LETTERS");

    private static String ruleIdentity(EveBlock block) {
        List<String> lines = block.lines;
        boolean isRule = false;
        for (String line : lines) {
            String t = line.trim();
            if (t.startsWith("HEAR") || t.startsWith("FIND")
                    || t.startsWith("MATCH") || t.startsWith("ANYWHERE")) {
                isRule = true;
                break;
            }
            if (!t.isEmpty()) {
                break;
            }
        }
        if (!isRule) {
            return block.key;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;
            String head = t;
            int paren = head.indexOf('(');
            int space = head.indexOf(' ');
            int cut = paren < 0 ? space : (space < 0 ? paren : Math.min(paren, space));
            if (cut > 0) head = head.substring(0, cut);
            if (EVE_RULE_MODIFIERS.contains(head.toUpperCase(java.util.Locale.ROOT))) {
                continue;
            }
            if (sb.length() > 0) sb.append('\n');
            sb.append(t.replaceAll("\\s+", " "));
        }
        return sb.toString();
    }

    private static String normalizeKey(List<String> block) {
        StringBuilder sb = new StringBuilder();
        for (String line : block) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;
            if (sb.length() > 0) sb.append('\n');
            sb.append(t.replaceAll("\\s+", " "));
        }
        return sb.toString();
    }

    private static String readResource(Plugin plugin, String name) {
        try (InputStream in = plugin.getResource(name)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

}

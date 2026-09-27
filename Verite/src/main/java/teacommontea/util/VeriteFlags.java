package teacommontea.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.plugin.Plugin;


public final class VeriteFlags {

    public enum Type { BOOLEAN, INTEGER, DECIMAL, ENUM }

    public static final class Flag {
        final String name;
        final String file;
        final String[] path;
        final Type type;
        final List<String> values;

        final String rawPath;

        Flag(String name, String file, String path, Type type, List<String> values) {
            this.name = name;
            this.file = file;
            this.rawPath = path;
            this.path = path.split("\\.");
            this.type = type;
            this.values = values;
        }

        public String name() { return name; }
        public Type type() { return type; }
        public String file() { return file; }

        public boolean liveReloadable() {
            return !isConfigGate();
        }

        public boolean isConfigGate() {
            return rawPath.equals("general.auto.update.config")
                    || rawPath.equals("chat.filter.auto.update.eve");
        }
    }

    private static final Map<String, Flag> FLAGS = new LinkedHashMap<>();

    private static void reg(String name, String file, String path, Type type, List<String> values) {
        FLAGS.put(name, new Flag(name, file, path, type, values));
    }

    private static final List<String> BOOL = List.of("true", "false");

    private static final String CFG = "config.yml";

    static {
        String adv = "chat.filter.advanced.";

        reg("keep.chat.readable", CFG, adv + "keep.chat.readable", Type.BOOLEAN, BOOL);
        reg("block.possible.spam", CFG, adv + "block.possible.spam", Type.BOOLEAN, BOOL);
        reg("block.unsupported.languages", CFG, adv + "block.unsupported.languages", Type.BOOLEAN, BOOL);

        reg("evasion.homoglyph", CFG, adv + "evasion.homoglyph", Type.BOOLEAN, BOOL);
        reg("evasion.entity", CFG, adv + "evasion.entity", Type.BOOLEAN, BOOL);
        reg("evasion.deobfuscate", CFG, adv + "evasion.deobfuscate", Type.BOOLEAN, BOOL);
        reg("evasion.segmentation", CFG, adv + "evasion.segmentation", Type.BOOLEAN, BOOL);
        reg("evasion.fingerprint", CFG, adv + "evasion.fingerprint", Type.BOOLEAN, BOOL);
        reg("use.profanity.confidence.enabled", CFG, adv + "use.profanity.confidence.enabled", Type.BOOLEAN, BOOL);
        reg("use.profanity.confidence.profanity.confidence.threshold", CFG,
                adv + "use.profanity.confidence.profanity.confidence.threshold", Type.DECIMAL, List.of("0.0..1.0"));

        reg("chat.filter.enabled", CFG, "chat.filter.enabled", Type.BOOLEAN, BOOL);
        reg("moderation.enabled", CFG, "moderation.enabled", Type.BOOLEAN, BOOL);
        reg("vanish.enabled", CFG, "vanish.enabled", Type.BOOLEAN, BOOL);
        reg("auto.update.config", CFG, "general.auto.update.config", Type.BOOLEAN, BOOL);
        reg("auto.update.eve", CFG, "chat.filter.auto.update.eve", Type.BOOLEAN, BOOL);

        reg("moderation.auto.ban.alts", CFG, "moderation.auto.ban.alts", Type.BOOLEAN, BOOL);
        reg("moderation.exempt.use.group.weights", CFG, "moderation.exempt.use.group.weights", Type.BOOLEAN, BOOL);
        reg("moderation.exempt.permit.same.weight", CFG, "moderation.exempt.permit.same.weight", Type.BOOLEAN, BOOL);
        reg("moderation.shared.ip.scan.limit", CFG, "moderation.shared.ip.scan.limit", Type.INTEGER, List.of("1..200"));

        String vf = "vanish.features.";
        for (String feat : new String[]{
                "fake.message", "actionbar", "self.view", "fly", "invulnerability", "effects",
                "gamemode", "ride.entity", "silent.container", "inventory.inspect", "server.ping",
                "ghost", "prevent.chat", "prevent.pickup", "prevent.drop", "prevent.interact",
                "prevent.block.break", "prevent.block.place", "prevent.target", "prevent.damage",
                "prevent.food", "prevent.buckets", "prevent.advancement", "prevent.projectiles",
                "muffle.sounds", "muffle.particles"}) {
            reg("vanish.features." + feat, CFG, vf + feat, Type.BOOLEAN, BOOL);
        }
    }

    private VeriteFlags() {}

    private static volatile Map<String, Flag> discovered = new LinkedHashMap<>();

    public static void discover(Plugin plugin) {
        Map<String, Flag> found = new LinkedHashMap<>();
        java.io.File f = new java.io.File(plugin.getDataFolder(), CFG);
        if (!f.isFile()) {
            discovered = found;
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            discovered = found;
            return;
        }
        for (Map.Entry<String, String> e : scalars(lines).entrySet()) {
            String dotted = e.getKey();
            Type type = inferType(e.getValue());
            if (type == null) continue;
            String name = dotted.toLowerCase(Locale.ROOT);
            if (FLAGS.containsKey(name)) continue;
            found.put(name, new Flag(name, CFG, dotted, type,
                    type == Type.BOOLEAN ? BOOL : List.of(kindHint(type))));
        }
        discovered = found;
    }

    private static Type inferType(String value) {
        String t = value.trim();
        if (t.isEmpty()) return null;
        if (t.startsWith("{") || t.startsWith("[")) return null;
        if (t.startsWith("\"") || t.startsWith("'")) return null;
        String low = t.toLowerCase(Locale.ROOT);
        if (low.equals("true") || low.equals("false")) return Type.BOOLEAN;
        if (t.indexOf('.') >= 0 && isNumeric(t)) return Type.DECIMAL;
        if (isNumeric(t)) return Type.INTEGER;
        return null;
    }

    private static boolean isNumeric(String t) {
        boolean digit = false;
        boolean dot = false;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (i == 0 && (c == '-' || c == '+')) continue;
            if (c == '.') {
                if (dot) return false;
                dot = true;
                continue;
            }
            if (c < '0' || c > '9') return false;
            digit = true;
        }
        return digit;
    }

    private static String kindHint(Type type) {
        return type == Type.DECIMAL ? "<decimal>" : "<integer>";
    }

    private static Map<String, String> scalars(List<String> lines) {
        Map<String, String> out = new LinkedHashMap<>();
        List<String> names = new ArrayList<>();
        List<Integer> indents = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) continue;
            int colon = Yaml.keyColon(line);
            if (colon < 0) continue;
            int indent = Yaml.indentOf(line);
            String key = line.substring(indent, colon).trim();
            if (key.isEmpty()) continue;
            while (!indents.isEmpty() && indents.get(indents.size() - 1) >= indent) {
                indents.remove(indents.size() - 1);
                names.remove(names.size() - 1);
            }
            names.add(key);
            indents.add(indent);
            String value = Yaml.valueOf(line);
            if (value != null && !value.isEmpty()) {
                out.put(String.join(".", names), value);
            }
        }
        return out;
    }

    public static List<String> names() {
        List<String> out = new ArrayList<>(FLAGS.keySet());
        out.addAll(discovered.keySet());
        return out;
    }

    public static Flag flag(String name) {
        if (name == null) return null;
        String key = name.toLowerCase(Locale.ROOT);
        Flag f = FLAGS.get(key);
        return f != null ? f : discovered.get(key);
    }

    public static List<String> suggest(Flag flag) {
        return flag == null ? List.of() : flag.values;
    }

    public static String get(Plugin plugin, Flag flag) {
        java.io.File f = new java.io.File(plugin.getDataFolder(), flag.file);
        if (!f.isFile()) {
            return null;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
        int idx = locate(lines, flag.path);
        if (idx < 0) {
            return null;
        }
        return Yaml.valueOf(lines.get(idx));
    }

    public static boolean set(Plugin plugin, Flag flag, String value) {
        if (flag == null || value == null || !validate(flag, value)) {
            return false;
        }
        java.io.File f = new java.io.File(plugin.getDataFolder(), flag.file);
        if (!f.isFile()) {
            return false;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return false;
        }
        int idx = locate(lines, flag.path);
        if (idx < 0) {
            return false;
        }
        String original = lines.get(idx);
        int colon = original.indexOf(':');
        if (colon < 0) {
            return false;
        }
        String before = original.substring(0, colon + 1);
        String comment = Yaml.trailingComment(original, colon + 1);
        String rebuilt = before + " " + value + (comment.isEmpty() ? "" : " " + comment);
        lines.set(idx, rebuilt);

        java.io.File tmp = new java.io.File(f.getParentFile(), f.getName() + ".tmp");
        try {
            Files.write(tmp.toPath(), (String.join("\n", lines) + "\n").getBytes(StandardCharsets.UTF_8));
            Files.move(tmp.toPath(), f.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            tmp.delete();
            return false;
        }
    }

    private static boolean validate(Flag flag, String value) {
        switch (flag.type) {
            case BOOLEAN:
                return value.equals("true") || value.equals("false");
            case INTEGER:
                try {
                    Long.parseLong(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case DECIMAL:
                try {
                    Double.parseDouble(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case ENUM:
                return flag.values.contains(value);
            default:
                return false;
        }
    }

    private static int locate(List<String> lines, String[] path) {
        return locate(lines, path, 0, 0, lines.size());
    }

    private static int locate(List<String> lines, String[] path, int from, int depth, int end) {

        int flat = findKey(lines, String.join(".", path), from, depth, end);
        if (flat >= 0) {
            return flat;
        }

        for (int take = path.length - 1; take >= 1; take--) {
            String blockKey = String.join(".", java.util.Arrays.copyOfRange(path, 0, take));
            int blockLine = findKey(lines, blockKey, from, depth, end);
            if (blockLine < 0) {
                continue;
            }
            int blockEnd = blockEnd(lines, blockLine, depth);
            String[] rest = java.util.Arrays.copyOfRange(path, take, path.length);
            int hit = locate(lines, rest, blockLine + 1, depth + 1, blockEnd);
            if (hit >= 0) {
                return hit;
            }
        }
        return -1;
    }

    private static int findKey(List<String> lines, String key, int from, int depth, int end) {
        for (int i = from; i < end && i < lines.size(); i++) {
            String stripped = Yaml.stripComment(lines.get(i));
            if (stripped.trim().isEmpty()) {
                continue;
            }
            if (Yaml.indentOf(stripped) != depth * 2) {
                continue;
            }
            String content = stripped.trim();
            if (content.equals(key + ":") || content.startsWith(key + ": ")
                    || content.startsWith(key + ":")) {
                return i;
            }
        }
        return -1;
    }

    private static int blockEnd(List<String> lines, int blockLine, int depth) {
        for (int i = blockLine + 1; i < lines.size(); i++) {
            String stripped = Yaml.stripComment(lines.get(i));
            if (stripped.trim().isEmpty()) {
                continue;
            }
            if (Yaml.indentOf(stripped) <= depth * 2) {
                return i;
            }
        }
        return lines.size();
    }
}

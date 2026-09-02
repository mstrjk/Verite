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

    public static List<String> names() {
        return new ArrayList<>(FLAGS.keySet());
    }

    public static Flag flag(String name) {
        return name == null ? null : FLAGS.get(name.toLowerCase(Locale.ROOT));
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

package teacommontea.dashboard;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.plugin.Plugin;

import teacommontea.util.Yaml;

public final class DashboardConfig {

    private static final char SEP = '\u0001';

    private DashboardConfig() {}

    public static String describe(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.isFile()) {
            return "null";
        }

        List<String> lines;
        try {
            lines = Yaml.splitLines(new String(
                    Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "null";
        }

        List<String> keys = new ArrayList<>();
        for (Node n : parse(lines)) {
            if (n.value == null) {
                continue;
            }
            keys.add(Json.object(
                    "path", n.path.replace(SEP, '.'),
                    "name", n.name,
                    "section", n.section.replace(SEP, '.'),
                    "value", n.value,
                    "kind", kind(n.value),
                    "comment", n.comment
            ));
        }

        return Json.object(
                "file", "config.yml",
                "keys", Json.raw(Json.array(keys))
        );
    }

    public static boolean set(Plugin plugin, String dotted, String value) throws Exception {
        if (dotted == null || value == null) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.isFile()) {
            return false;
        }

        String raw = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String eol = raw.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = Yaml.splitLines(raw);

        Node target = null;
        for (Node n : parse(lines)) {
            if (n.value != null && n.path.replace(SEP, '.').equals(dotted)) {
                target = n;
                break;
            }
        }
        if (target == null) {
            return false;
        }

        if (shape(target.value) != shape(value)) {
            throw new IllegalArgumentException(
                    "That is the wrong kind of value for " + dotted
                    + ". It expects " + kind(target.value) + ".");
        }

        lines.set(target.line, replaceValue(lines.get(target.line), value));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.get(i));
            if (i < lines.size() - 1) {
                sb.append(eol);
            }
        }
        if (raw.endsWith("\n") || raw.endsWith("\r")) {
            sb.append(eol);
        }

        File backup = new File(plugin.getDataFolder(), "config.yml.bak");
        Files.copy(file.toPath(), backup.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        return true;
    }

    private static String replaceValue(String line, String value) {
        int colon = Yaml.keyColon(line);
        if (colon < 0) {
            return line;
        }
        String after = line.substring(colon + 1);
        int hash = Yaml.commentStartOutsideQuotes(after);
        String trailing = hash >= 0 ? after.substring(hash) : "";
        int lead = 0;
        while (lead < after.length() && after.charAt(lead) == ' ') {
            lead++;
        }
        String leadSpace = after.substring(0, Math.max(1, lead));
        String trailSpace = trailing.isEmpty() ? "" : " ";
        return line.substring(0, colon + 1) + leadSpace + value + trailSpace + trailing;
    }

    private static String kind(String value) {
        return switch (shape(value)) {
            case 1 -> "braces";
            case 2 -> "list";
            case 3 -> "boolean";
            case 4 -> "number";
            default -> "text";
        };
    }

    private static int shape(String value) {
        String t = value.trim();
        if (t.isEmpty()) {
            return 0;
        }
        if (t.startsWith("{") && t.endsWith("}")) {
            return 1;
        }
        if (t.startsWith("[") && t.endsWith("]")) {
            return 2;
        }
        String low = t.toLowerCase(Locale.ROOT);
        if (low.equals("true") || low.equals("false")) {
            return 3;
        }
        if (isNumber(t)) {
            return 4;
        }
        return 6;
    }

    private static boolean isNumber(String t) {
        boolean dot = false;
        boolean digit = false;
        int i = 0;
        if (i < t.length() && (t.charAt(i) == '-' || t.charAt(i) == '+')) {
            i++;
        }
        for (; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '.') {
                if (dot) {
                    return false;
                }
                dot = true;
            } else if (c >= '0' && c <= '9') {
                digit = true;
            } else {
                return false;
            }
        }
        return digit;
    }

    private static List<Node> parse(List<String> lines) {
        List<Node> nodes = new ArrayList<>();
        List<int[]> stack = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) {
                continue;
            }

            int colon = Yaml.keyColon(line);
            if (colon < 0) {
                continue;
            }

            int indent = Yaml.indentOf(line);
            String name = line.substring(indent, colon).trim();
            if (name.isEmpty()) {
                continue;
            }

            while (!stack.isEmpty() && stack.get(stack.size() - 1)[0] >= indent) {
                stack.remove(stack.size() - 1);
            }

            StringBuilder pathB = new StringBuilder();
            StringBuilder sectionB = new StringBuilder();
            for (int[] anc : stack) {
                if (pathB.length() > 0) {
                    pathB.append(SEP);
                    sectionB.append(SEP);
                }
                pathB.append(nodes.get(anc[1]).name);
                sectionB.append(nodes.get(anc[1]).name);
            }
            if (pathB.length() > 0) {
                pathB.append(SEP);
            }
            pathB.append(name);

            Node node = new Node(pathB.toString(), name, sectionB.toString(),
                    i, value(line), comment(lines, i));
            nodes.add(node);
            stack.add(new int[] { indent, nodes.size() - 1 });
        }

        return nodes;
    }

    private static String value(String line) {
        int colon = Yaml.keyColon(line);
        if (colon < 0) {
            return null;
        }
        String after = line.substring(colon + 1);
        int hash = Yaml.commentStartOutsideQuotes(after);
        if (hash >= 0) {
            after = after.substring(0, hash);
        }
        String v = after.trim();
        return v.isEmpty() ? null : v;
    }

    private static String comment(List<String> lines, int keyLine) {
        List<String> parts = new ArrayList<>();
        for (int i = keyLine - 1; i >= 0; i--) {
            String t = lines.get(i).trim();
            if (t.startsWith("#")) {
                String text = t.substring(1).trim();
                if (!text.isEmpty() && !text.startsWith("----")) {
                    parts.add(0, text);
                }
                continue;
            }
            break;
        }
        return parts.isEmpty() ? null : String.join(" ", parts);
    }

    private static final class Node {
        final String path;
        final String name;
        final String section;
        final int line;
        final String value;
        final String comment;

        Node(String path, String name, String section, int line, String value, String comment) {
            this.path = path;
            this.name = name;
            this.section = section;
            this.line = line;
            this.value = value;
            this.comment = comment;
        }
    }
}

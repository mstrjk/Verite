package teacommontea.veritesauver.punish;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.util.SauverDuration;
import teacommontea.veritesauver.util.SauverConfig;

public final class SauverTree {

    public enum StepType { WARN, MUTE, KICK, BAN }

    public record Step(StepType type, long durationMillis, String durationText) {
        public boolean permanent() {
            return durationMillis == Entry.PERMANENT;
        }
    }

    public static final class Category {
        private final String name;
        private final List<Step> ladder;
        private final Material icon;

        Category(String name, List<Step> ladder, Material icon) {
            this.name = name;
            this.ladder = ladder;
            this.icon = icon;
        }

        public String name()      { return name; }
        public List<Step> ladder(){ return ladder; }
        public Material icon()    { return icon; }

        public Step stepFor(int priorOffences) {
            if (ladder.isEmpty()) {
                return null;
            }
            int idx = Math.max(0, Math.min(priorOffences, ladder.size() - 1));
            return ladder.get(idx);
        }
    }

    private final boolean enabled;
    private final Map<String, Category> categories;
    private final Map<String, List<String>> permissions;

    private SauverTree(boolean enabled, Map<String, Category> categories,
                       Map<String, List<String>> permissions) {
        this.enabled = enabled;
        this.categories = categories;
        this.permissions = permissions;
    }

    public boolean enabled()                       { return enabled; }
    public Category category(String name)          { return categories.get(name); }
    public Map<String, Category> categories()      { return categories; }
    public Map<String, List<String>> permissions() { return permissions; }

    public static SauverTree load() {
        YamlConfiguration y = SauverConfig.yaml();
        String base = "moderation.tree";
        boolean enabled = y.getBoolean(base + ".enabled", false);

        Map<String, Category> cats = new LinkedHashMap<>();
        ConfigurationSection catSec = y.getConfigurationSection(base + ".categories");
        if (catSec != null) {
            for (String key : catSec.getKeys(true)) {
                Object raw = catSec.get(key);
                if (!(raw instanceof String s)) {
                    continue;
                }
                Category c = parseCategory(key, unbrace(s));
                if (c != null) {
                    cats.put(key, c);
                }
            }
        }

        Map<String, List<String>> perms = new LinkedHashMap<>();
        ConfigurationSection lpSec = y.getConfigurationSection(base + ".permissions.luckperms");
        if (lpSec != null) {
            List<String> inherited = new ArrayList<>();
            for (String rank : lpSec.getKeys(false)) {
                Object raw = lpSec.get(rank);
                List<String> own = raw instanceof String s ? splitList(unbrace(s)) : List.of();
                List<String> resolved = new ArrayList<>(inherited);
                for (String cat : own) {
                    if (!resolved.contains(cat)) {
                        resolved.add(cat);
                    }
                }
                perms.put(rank.toLowerCase(Locale.ROOT), resolved);
                inherited = resolved;
            }
        }

        return new SauverTree(enabled, cats, perms);
    }

    private static String unbrace(String v) {
        String t = v.trim();
        if (t.length() >= 2 && t.charAt(0) == '{' && t.charAt(t.length() - 1) == '}') {
            return t.substring(1, t.length() - 1);
        }
        return t;
    }

    private static List<String> splitList(String body) {
        List<String> out = new ArrayList<>();
        for (String piece : body.split(",")) {
            String p = piece.trim();
            if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    private static Category parseCategory(String name, String body) {
        List<Step> ladder = new ArrayList<>();
        Material icon = null;
        for (String token : body.split(",")) {
            String t = token.trim();
            if (t.isEmpty()) {
                continue;
            }
            String lower = t.toLowerCase(Locale.ROOT);
            if (lower.startsWith("minecraft:")) {
                Material m = Material.matchMaterial(t);
                if (m != null && m.isItem()) {
                    icon = m;
                }
                continue;
            }
            Step step = parseStep(lower);
            if (step != null) {
                ladder.add(step);
            }
        }
        if (ladder.isEmpty()) {
            return null;
        }
        if (icon == null) {
            icon = Material.PAPER;
        }
        return new Category(name, ladder, icon);
    }

    private static Step parseStep(String token) {
        String verb = token;
        long duration = Entry.PERMANENT;
        String durText = "";
        int colon = token.indexOf(':');
        if (colon >= 0) {
            verb = token.substring(0, colon).trim();
            durText = token.substring(colon + 1).trim();
            long parsed = SauverDuration.parse(durText);
            if (parsed > 0) {
                duration = parsed;
            } else {
                durText = "";
            }
        }
        return switch (verb) {
            case "warn" -> new Step(StepType.WARN, Entry.PERMANENT, "");
            case "kick" -> new Step(StepType.KICK, Entry.PERMANENT, "");
            case "mute" -> new Step(StepType.MUTE, duration, durText);
            case "ban"  -> new Step(StepType.BAN, duration, durText);
            default -> null;
        };
    }
}

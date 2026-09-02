package teacommontea.veritesauver.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import teacommontea.util.Yaml;

public final class SauverConfig {

    private static volatile YamlConfiguration yml = new YamlConfiguration();

    private SauverConfig() {}

    public static void load(File dataFolder) {
        yml = Yaml.loadYaml(new File(dataFolder, "config.yml"));
    }

    private static String unwrapBraces(String v) {
        if (v != null && v.length() >= 2 && v.charAt(0) == '{' && v.charAt(v.length() - 1) == '}') {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    public static boolean moderationEnabled() {
        return yml.getBoolean("moderation.enabled", true);
    }

    public static boolean banAlts() {
        return yml.getBoolean("moderation.auto.ban.alts", false);
    }

    public static boolean treeUseFields() {
        return yml.getBoolean("moderation.use.fields", false);
    }

    public static List<String> muteCommandBlacklist() {
        List<String> def = List.of(
                "me", "say", "tell", "whisper",
                "reply", "pm", "message", "msg",
                "msgall", "tellall", "speak", "emsg",
                "epm", "etell", "ewhisper", "w",
                "m", "t", "r", "mail");
        List<String> got = yml.getStringList("moderation.muted.command.blacklist");
        return got.isEmpty() ? def : got;
    }

    public static String template(String key, String def) {
        String v = yml.getString("moderation.punish.message.templates." + key, null);
        if (v == null || v.isEmpty()) return def;
        return unwrapBraces(v);
    }

    public static long warningExpire() {
        long d = SauverDuration.parse(yml.getString("moderation.expire.warns.after", "7d"));
        return d <= 0 ? 7L * SauverDuration.DAY : d;
    }

    public static boolean useGroupWeights() {
        return yml.getBoolean("moderation.use.rank.weights", false)
                || yml.getBoolean("moderation.exempt.use.group.weights", false);
    }

    public static boolean permitSameWeight() {
        return yml.getBoolean("moderation.exempt.permit.same.weight", true);
    }

    public static int dupeipScanLimit() {
        return yml.getInt("moderation.shared.ip.scan.limit", 20);
    }

    public static YamlConfiguration yaml() {
        return yml;
    }
}

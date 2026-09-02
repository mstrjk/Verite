package teacommontea.api;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;


import teacommontea.veritesauver.util.SauverConfig;

public final class VeriteConfig {

    private VeriteConfig() {}

    public static boolean banAlts() {
        return SauverConfig.banAlts();
    }

    public static List<String> muteCommandBlacklist() {
        return SauverConfig.muteCommandBlacklist();
    }

    public static long warningExpire() {
        return SauverConfig.warningExpire();
    }

    public static boolean useGroupWeights() {
        return SauverConfig.useGroupWeights();
    }

    public static boolean permitSameWeight() {
        return SauverConfig.permitSameWeight();
    }

    public static boolean reduceToLimit() {
        return true;
    }

    public static int dupeipScanLimit() {
        return SauverConfig.dupeipScanLimit();
    }

    public static YamlConfiguration yaml() {
        return SauverConfig.yaml();
    }
}

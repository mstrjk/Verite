package teacommontea.api;

import java.util.List;
import java.util.UUID;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.client.ClientDetect;
import teacommontea.veritesauver.client.ClientProfile;
import teacommontea.veritesauver.client.ClientVerdict;

public final class VeriteClient {

    private VeriteClient() {}

    private static ClientDetect detect() {
        Sauver s = Sauver.instance();
        return s == null ? null : s.clientDetect();
    }

    public static boolean enabled() {
        return detect() != null;
    }

    public static String identityOf(UUID player) {
        ClientProfile profile = liveProfile(player);
        if (profile != null && profile.identity() != null) {
            return profile.identity();
        }
        Sauver s = Sauver.instance();
        return s == null ? null : s.dao().clientIdentity(player);
    }

    public static int confidenceOf(UUID player) {
        ClientProfile profile = liveProfile(player);
        if (profile != null && profile.identity() != null) {
            return profile.confidence();
        }
        Sauver s = Sauver.instance();
        return s == null ? 0 : s.dao().clientConfidence(player);
    }

    public static boolean identified(UUID player) {
        ClientProfile profile = liveProfile(player);
        return profile != null && ClientVerdict.confident(profile);
    }

    public static boolean misreportsBrand(UUID player) {
        ClientProfile profile = liveProfile(player);
        return profile != null && ClientVerdict.contradictsVanilla(profile);
    }

    public static String reportedBrandOf(UUID player) {
        ClientProfile profile = liveProfile(player);
        return profile == null ? null : profile.reportedBrand();
    }

    public static List<String> modsOf(UUID player) {
        ClientProfile profile = liveProfile(player);
        if (profile != null) {
            return ClientVerdict.mods(profile);
        }
        Sauver s = Sauver.instance();
        String stored = s == null ? null : s.dao().clientMods(player);
        return stored == null || stored.isBlank() ? List.of() : List.of(stored.split(","));
    }

    public static List<String> anomaliesOf(UUID player) {
        ClientProfile profile = liveProfile(player);
        return profile == null ? List.of() : ClientVerdict.anomalies(profile);
    }

    public static List<String> evidenceOf(UUID player) {
        ClientDetect d = detect();
        if (d == null) {
            return List.of();
        }
        return d.signalsOf(player).stream().map(s -> s.source() + ": " + s.describe()).toList();
    }

    private static ClientProfile liveProfile(UUID player) {
        ClientDetect d = detect();
        return d == null ? null : d.profile(player);
    }
}

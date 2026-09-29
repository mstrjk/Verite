package teacommontea.veritesauver.client;

import org.bukkit.entity.Player;

import java.util.List;

import teacommontea.util.Colours;
import teacommontea.util.Lang;

public final class ClientNotice {

    private ClientNotice() {}

    public static String hover(Player p, ClientProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append(Lang.of("login.client.label", "client", profile.identity()));

        int confidence = profile.confidence();
        if (confidence > 0) {
            sb.append("<newline>").append(Lang.of("client.confidence", "percent", confidence));
        }

        String reported = profile.reportedBrand();
        if (reported != null && !reported.isBlank()) {
            sb.append("<newline>").append(Lang.of("client.reported", "brand",
                    teacommontea.util.text.Text.capitalise(reported)));
        }

        List<String> mods = ClientVerdict.mods(profile);
        if (!mods.isEmpty()) {
            sb.append("<newline>").append(Lang.of("client.detected",
                    "list", String.join(Colours.BRAND_ACCENT_SECONDARY + ", " + Colours.BRAND, mods)));
        }

        List<String> unknown = ClientVerdict.unknownChannels(profile);
        if (!unknown.isEmpty()) {
            sb.append("<newline>").append(Lang.of("client.unknown.channels",
                    "list", String.join(Colours.BRAND_ACCENT_SECONDARY + ", " + Colours.BRAND, unknown)));
        }

        List<String> anomalies = ClientVerdict.anomalies(profile);
        if (!anomalies.isEmpty()) {
            sb.append("<newline>").append(Lang.of("client.anomalies",
                    "list", String.join(Colours.BRAND_ACCENT_SECONDARY + ", " + Colours.WARN, anomalies)));
        }

        for (String line : profile.facts().values()) {
            sb.append("<newline>").append(line);
        }
        return sb.toString().replace("'", "’");
    }

    public static String line(Player p, ClientProfile profile) {
        String key = ClientVerdict.contradictsVanilla(profile)
                ? "client.brand.disputed" : "login.client.brand";
        return Lang.of(key, "name", p.getName(),
                "client", "<hover:show_text:'" + hover(p, profile) + "'>"
                        + profile.identity() + "</hover>");
    }

    public static boolean worthReporting(ClientProfile profile) {
        if (ClientConfig.notifyVanilla()) {
            return true;
        }
        if (!profile.signals().isEmpty()) {
            return true;
        }
        String reported = profile.reportedBrand();
        return reported != null && !reported.isBlank() && !reported.equalsIgnoreCase("vanilla");
    }
}

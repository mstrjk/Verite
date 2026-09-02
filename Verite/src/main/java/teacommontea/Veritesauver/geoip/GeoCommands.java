package teacommontea.veritesauver.geoip;

import teacommontea.util.Colours;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.command.CommandBase;

public final class GeoCommands extends CommandBase {

    public GeoCommands(Sauver sauver) {
        super(sauver);
    }

    public void geoip(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "geoip <player|IP>", "look up the country for a player or IP");
            return;
        }
        String ip;
        if (isIpLiteral(args[0])) {
            ip = args[0];
        } else {
            UUID u = resolve(args[0]);
            if (u == null) {
                unknownPlayer(sender, args[0]);
                return;
            }
            List<String> ips = dao().ipsOf(u);
            ip = ips.isEmpty() ? null : ips.get(0);
        }
        if (ip == null) {
            err(sender, Colours.WARNING + "No IP on record for that player.");
            return;
        }
        String country = SauverGeoIp.country(ip);
        if (country == null) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "GeoIP for " + Colours.BRAND_ACCENT_SECONDARY + ip
                    + Colours.BRAND_ACCENT_SECONDARY + ": unavailable (no GeoLite2 database configured).");
            return;
        }
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "GeoIP for " + Colours.BRAND_ACCENT_SECONDARY + ip + Colours.BRAND_ACCENT_SECONDARY + ": " + Colours.BRAND_ACCENT_SECONDARY + country);
    }
}

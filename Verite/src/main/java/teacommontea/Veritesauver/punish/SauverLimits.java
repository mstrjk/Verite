package teacommontea.veritesauver.punish;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.util.SauverDuration;

public final class SauverLimits {

    private record Tier(String permission, long maxBan, long maxMute, long cooldown) {}

    private static final List<Tier> TIERS = List.of(
        new Tier("veritesauver.group.moderator", SauverDuration.MONTH, SauverDuration.MONTH, 20_000),
        new Tier("veritesauver.group.helper", 15 * SauverDuration.DAY, 7 * SauverDuration.DAY, 20_000),
        new Tier("", 7 * SauverDuration.DAY, SauverDuration.DAY, 20_000)
    );

    private static final Map<UUID, Map<String, Long>> LAST = new java.util.concurrent.ConcurrentHashMap<>();

    private SauverLimits() {}

    public record Check(boolean ok, long durationMillis, String error) {
        static Check reject(String why) { return new Check(false, 0, why); }
        static Check allow(long d)       { return new Check(true, d, null); }
    }

    public static Check capDuration(CommandSender issuer, Entry.Type type, long requested) {
        if (!(issuer instanceof Player p) || issuer.hasPermission("veritesauver.admin")) {
            return Check.allow(requested);
        }
        Tier tier = tierOf(p);
        long cap = type == Entry.Type.BAN ? tier.maxBan() : tier.maxMute();
        boolean overCap = requested == Entry.PERMANENT || requested > cap;
        if (!overCap) {
            return Check.allow(requested);
        }

        return Check.allow(cap);
    }

    public static long cooldownRemaining(CommandSender issuer, Entry.Type type, long now) {
        if (!(issuer instanceof Player p) || issuer.hasPermission("veritesauver.admin")) {
            return 0;
        }
        long cd = tierOf(p).cooldown();
        Long last = LAST.getOrDefault(p.getUniqueId(), Map.of()).get(type.id());
        if (last == null) {
            return 0;
        }
        long elapsed = now - last;
        return elapsed >= cd ? 0 : cd - elapsed;
    }

    public static void markUsed(CommandSender issuer, Entry.Type type, long now) {
        if (issuer instanceof Player p) {
            LAST.computeIfAbsent(p.getUniqueId(), k -> new java.util.concurrent.ConcurrentHashMap<>()).put(type.id(), now);
        }
    }

    private static Tier tierOf(Player p) {
        for (Tier t : TIERS) {
            if (t.permission().isEmpty() || p.hasPermission(t.permission())) {
                return t;
            }
        }
        return TIERS.get(TIERS.size() - 1);
    }
}

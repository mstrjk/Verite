package teacommontea.api;

import org.bukkit.command.CommandSender;

import teacommontea.api.internal.ApiBridge;

import teacommontea.veritesauver.punish.SauverLimits;

public final class VeriteLimits {

    private VeriteLimits() {}

    public static DurationCheck capDuration(CommandSender issuer, PunishmentType type, long requested) {
        return ApiBridge.toCheck(SauverLimits.capDuration(issuer, ApiBridge.fromType(type), requested));
    }

    public static long cooldownRemaining(CommandSender issuer, PunishmentType type, long now) {
        return SauverLimits.cooldownRemaining(issuer, ApiBridge.fromType(type), now);
    }

    public static void markUsed(CommandSender issuer, PunishmentType type, long now) {
        SauverLimits.markUsed(issuer, ApiBridge.fromType(type), now);
    }
}

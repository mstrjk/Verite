package teacommontea.api;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import teacommontea.api.internal.ApiBridge;

import teacommontea.veritesauver.punish.SauverExempt;

public final class VeriteExempt {

    private VeriteExempt() {}

    public static String blockReason(CommandSender issuer, UUID target, PunishmentType type) {
        return SauverExempt.blockReason(issuer, target, ApiBridge.fromType(type));
    }
}

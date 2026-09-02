package teacommontea.api;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import teacommontea.api.internal.ApiBridge;

import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.core.SauverEngine;

public final class VeriteModeration {

    private VeriteModeration() {}

    private static Sauver sauver() {
        return Sauver.instance();
    }

    public static boolean enabled() {
        return sauver() != null;
    }

    public static boolean isBanned(UUID player) {
        return enabled() && sauver().activeBan(player) != null;
    }

    public static boolean isMuted(UUID player) {
        return enabled() && sauver().activeMute(player) != null;
    }

    public static Punishment activeBan(UUID player) {
        return enabled() ? ApiBridge.toPunishment(sauver().activeBan(player)) : null;
    }

    public static Punishment activeMute(UUID player) {
        return enabled() ? ApiBridge.toPunishment(sauver().activeMute(player)) : null;
    }

    public static List<Punishment> history(UUID player, int limit) {
        return enabled() ? ApiBridge.toPunishments(sauver().dao().history(player, limit)) : List.of();
    }

    public static List<Punishment> byIp(String ip, int limit) {
        return enabled() ? ApiBridge.toPunishments(sauver().dao().byIp(ip, limit)) : List.of();
    }

    public static List<Punishment> byStaff(UUID staff, int limit) {
        return enabled() ? ApiBridge.toPunishments(sauver().dao().byStaff(staff, limit)) : List.of();
    }

    public static List<Punishment> activeWarnings(UUID player) {
        return enabled()
                ? ApiBridge.toPunishments(sauver().dao().activeWarnings(player, System.currentTimeMillis()))
                : List.of();
    }

    public static List<ActivePunishment> activePunishments() {
        return enabled() ? ApiBridge.toActives(sauver().dao().activePointers()) : List.of();
    }

    public static List<Punishment> activeOfType(PunishmentType type, long now) {
        if (!enabled()) {
            return List.of();
        }
        return ApiBridge.toPunishments(sauver().dao().activeOfType(ApiBridge.fromType(type), now));
    }

    public static Punishment loadPunishment(long id) {
        return enabled() ? ApiBridge.toPunishment(sauver().dao().load(id)) : null;
    }

    public static List<UUID> altsOf(UUID player) {
        if (!enabled()) {
            return List.of();
        }
        List<String> ips = sauver().dao().ipsOf(player);
        if (ips.isEmpty()) {
            return List.of();
        }
        return sauver().dao().usersOfIp(ips.get(0));
    }

    public static int pruneHistory(UUID player, long cutoff, long now) {
        return enabled() ? sauver().dao().pruneHistory(player, cutoff, now) : 0;
    }

    public static PunishmentResult ban(UUID target, String targetName, String reason,
                                       UUID executor, String executorName, long durationMillis, boolean silent) {
        if (!enabled()) {
            return disabled();
        }
        return ApiBridge.toResult(SauverEngine.issue(Entry.Type.BAN, target, null, targetName,
                reason, executor, executorName, durationMillis, silent, false));
    }

    public static PunishmentResult mute(UUID target, String targetName, String reason,
                                        UUID executor, String executorName, long durationMillis, boolean silent) {
        if (!enabled()) {
            return disabled();
        }
        return ApiBridge.toResult(SauverEngine.issue(Entry.Type.MUTE, target, null, targetName,
                reason, executor, executorName, durationMillis, silent, false));
    }

    public static PunishmentResult ipban(UUID target, String ip, String targetName, String reason,
                                         UUID executor, String executorName, long durationMillis, boolean silent) {
        if (!enabled()) {
            return disabled();
        }
        return ApiBridge.toResult(SauverEngine.issueIp(Entry.Type.BAN, target, ip, targetName,
                reason, executor, executorName, durationMillis, silent));
    }

    public static PunishmentResult ipmute(UUID target, String ip, String targetName, String reason,
                                          UUID executor, String executorName, long durationMillis, boolean silent) {
        if (!enabled()) {
            return disabled();
        }
        return ApiBridge.toResult(SauverEngine.issueIp(Entry.Type.MUTE, target, ip, targetName,
                reason, executor, executorName, durationMillis, silent));
    }

    public static PunishmentResult warn(UUID target, String targetName, String reason,
                                        UUID executor, String executorName, boolean silent) {
        if (!enabled()) {
            return disabled();
        }
        return ApiBridge.toResult(SauverEngine.warn(target, targetName, reason, executor, executorName, silent));
    }

    public static PunishmentResult unwarn(UUID target, String targetName, UUID removedBy,
                                          String removedByName, String reason) {
        if (!enabled()) {
            return disabled();
        }
        return ApiBridge.toResult(SauverEngine.unwarn(target, targetName, removedBy, removedByName, reason));
    }

    public static PunishmentResult kick(UUID target, String targetName, String reason,
                                        UUID executor, String executorName, boolean silent) {
        if (!enabled()) {
            return disabled();
        }
        return ApiBridge.toResult(SauverEngine.kick(target, targetName, reason, executor, executorName, silent));
    }

    public static boolean unban(UUID target, String targetName, UUID remover, String removerName, String reason) {
        return enabled()
                && SauverEngine.pardon(Entry.Type.BAN, target, targetName, remover, removerName, reason).ok();
    }

    public static boolean unmute(UUID target, String targetName, UUID remover, String removerName, String reason) {
        return enabled()
                && SauverEngine.pardon(Entry.Type.MUTE, target, targetName, remover, removerName, reason).ok();
    }

    public static String banScreen(Punishment ban) {
        return enabled() && ban != null ? SauverEngine.banScreen(ApiBridge.fromPunishment(ban)) : null;
    }

    public static String muteLine(Punishment mute) {
        return enabled() && mute != null ? SauverEngine.muteLine(ApiBridge.fromPunishment(mute)) : null;
    }

    public static String muteNotice(Punishment mute) {
        return enabled() && mute != null ? SauverEngine.muteNotice(ApiBridge.fromPunishment(mute)) : null;
    }

    public static long warningExpire() {
        return SauverEngine.warningExpire();
    }

    public static boolean muteGate(Player p) {
        if (!enabled()) {
            return false;
        }
        Entry mute = sauver().activeMute(p.getUniqueId());
        if (mute == null) {
            return false;
        }
        sauver().messages().send(p, SauverEngine.muteNotice(mute));
        return true;
    }

    public static boolean chatMuteGate(Player p) {
        return enabled() && sauver().chat().mutedGate(p);
    }

    public static boolean slowmodeGate(Player p) {
        return enabled() && sauver().chat().slowmodeGate(p);
    }

    public static void broadcast(CommandSender sender, String[] args) {
        if (enabled()) {
            sauver().chat().broadcast(sender, args);
        }
    }

    public static void chatClear(CommandSender sender) {
        if (enabled()) {
            sauver().chat().chatClear(sender);
        }
    }

    public static void chatMute(CommandSender sender) {
        if (enabled()) {
            sauver().chat().chatMute(sender);
        }
    }

    public static void slowmode(CommandSender sender, String[] args) {
        if (enabled()) {
            sauver().chat().slowmode(sender, args);
        }
    }

    public static void registerListener(ModerationListener listener) {
        ApiBridge.registerModeration(listener);
    }

    public static void unregisterListener(ModerationListener listener) {
        ApiBridge.unregisterModeration(listener);
    }

    private static PunishmentResult disabled() {
        return new PunishmentResult(false, null, "moderation module disabled");
    }
}

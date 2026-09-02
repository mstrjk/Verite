package teacommontea.veritesauver.chat;

import teacommontea.util.Colours;
import teacommontea.util.Scope;
import teacommontea.veritesauver.util.SauverFormat;
import teacommontea.util.Messages;
import teacommontea.veritesauver.util.SauverMessages;

import org.bukkit.Bukkit;
import teacommontea.util.text.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.util.SauverDuration;

public final class SauverChat {

    private static final String BYPASS = "veritesauver.chat.bypass";

    private static final long MAX_SLOWMODE = 24L * SauverDuration.HOUR;

    private final Sauver sauver;

    public SauverChat(Sauver sauver) {
        this.sauver = sauver;
    }

    private SauverMessages msg() {
        return sauver.messages();
    }

    private Scope chat() {
        return sauver.store().scope("chatmod");
    }

    public void broadcast(CommandSender sender, String[] args) {
        if (args.length == 0) {
            msg().send(sender, Colours.BRAND_ACCENT_SECONDARY + "Usage: " + Colours.BRAND_ACCENT_SECONDARY + "/bc <message> " + Colours.BRAND_ACCENT_SECONDARY + "- broadcast a message to the server");
            return;
        }
        String message = String.join(" ", args);
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(teacommontea.util.text.Text.toLegacy(Messages.prefix()
                + Colours.BRAND_ACCENT_SECONDARY + ": " + Colours.BRAND_ACCENT_SECONDARY + message));
        Bukkit.broadcastMessage(" ");
    }

    public void chatClear(CommandSender sender) {
        for (int i = 0; i < 300; i++) {
            Bukkit.broadcastMessage(" ");
        }
        String who = sender instanceof Player p ? p.getName() : SauverEngine.CONSOLE_NAME;
        Text.broadcast(SauverMessages.screen(Colours.BRAND_ACCENT + "[" + Colours.WARNING + "!" + Colours.BRAND_ACCENT + "] " + Colours.BRAND_ACCENT_SECONDARY + "Chat was cleared by "
                + who + " " + Colours.BRAND_ACCENT + "[" + Colours.WARNING + "!" + Colours.BRAND_ACCENT + "]"));
    }

    public void chatMute(CommandSender sender) {
        boolean muted = isChatMuted();
        if (!muted) {
            chat().set("muted", true);
            Text.broadcast(SauverMessages.screen(Colours.BRAND_ACCENT + "[" + Colours.WARNING + "!" + Colours.BRAND_ACCENT + "]" + Colours.WARNING + " Chat has been muted! " + Colours.BRAND_ACCENT + "[" + Colours.WARNING + "!" + Colours.BRAND_ACCENT + "]"));
        } else {
            chat().set("muted", false);
            Text.broadcast(SauverMessages.screen(Colours.BRAND_ACCENT + "[" + Colours.WARNING + "!" + Colours.BRAND_ACCENT + "]" + Colours.BRAND + " Chat has been unmuted! " + Colours.BRAND_ACCENT + "[" + Colours.WARNING + "!" + Colours.BRAND_ACCENT + "]"));
        }
    }

    private boolean isChatMuted() {
        return chat().getBoolean("muted", false);
    }

    public boolean mutedGate(Player p) {
        if (p.hasPermission(BYPASS)) {
            return false;
        }
        if (isChatMuted()) {
            msg().err(p, Colours.WARNING + "You cannot speak while the chat is muted.");
            return true;
        }
        return false;
    }

    public void slowmode(CommandSender sender, String[] args) {
        String arg1 = args.length > 0 ? args[0] : null;
        String targetName = args.length > 1 ? args[1] : null;
        if (arg1 == null) {
            if (chat().has("global")) {
                chat().delete("global");
                Text.broadcast(SauverMessages.screen(Messages.prefix() + " " + Colours.BRAND + "Slowmode has been disabled."));
            } else {
                msg().send(sender, Colours.BRAND_ACCENT_SECONDARY + "Usage: " + Colours.BRAND_ACCENT_SECONDARY + "/slowmode <duration> [player] " + Colours.BRAND_ACCENT_SECONDARY + "- set chat slowmode");
            }
            return;
        }
        if (arg1.equalsIgnoreCase("off")) {
            if (targetName != null) {
                OfflinePlayer t = Bukkit.getOfflinePlayer(targetName);
                chat().delete("player." + t.getUniqueId());
                msg().send(sender, Colours.BRAND + "You removed slowmode from " + Colours.BRAND_ACCENT_SECONDARY + targetName + Colours.BRAND + ".");
                if (t.isOnline() && t.getPlayer() != null) {
                    msg().send(t.getPlayer(), Colours.BRAND_ACCENT_SECONDARY + "Your personal slowmode has been removed.");
                }
            } else {
                chat().delete("global");
                Text.broadcast(SauverMessages.screen(Messages.prefix() + " " + Colours.BRAND + "Slowmode has been disabled."));
            }
            return;
        }
        long millis = SauverDuration.parseShort(arg1);
        if (millis < 0) {
            msg().err(sender, Colours.WARNING + "That duration is invalid. " + Colours.BRAND_ACCENT_SECONDARY + "Use " + Colours.BRAND_ACCENT_SECONDARY + "s" + Colours.BRAND_ACCENT_SECONDARY + ", " + Colours.BRAND_ACCENT_SECONDARY + "m" + Colours.BRAND_ACCENT_SECONDARY + ", "
                    + Colours.BRAND_ACCENT_SECONDARY + "h" + Colours.BRAND_ACCENT_SECONDARY + ", or " + Colours.BRAND_ACCENT_SECONDARY + "d" + Colours.BRAND_ACCENT_SECONDARY + ". Example: " + Colours.BRAND_ACCENT_SECONDARY + "/slowmode 10s" + Colours.BRAND_ACCENT_SECONDARY + ".");
            return;
        }
        if (millis <= 0) {
            msg().err(sender, Colours.WARNING + "The duration must be greater than " + Colours.BRAND_ACCENT_SECONDARY + "0" + Colours.WARNING + ".");
            return;
        }
        if (millis > MAX_SLOWMODE) {
            msg().err(sender, Colours.WARNING + "Slowmode cannot be longer than " + Colours.BRAND_ACCENT_SECONDARY + "24h" + Colours.WARNING + ".");
            return;
        }
        if (targetName != null) {
            OfflinePlayer t = Bukkit.getOfflinePlayer(targetName);
            chat().set("player." + t.getUniqueId(), millis);
            msg().send(sender, Colours.BRAND_ACCENT_SECONDARY + "You set a slowmode of " + Colours.BRAND_ACCENT_SECONDARY + arg1 + " " + Colours.BRAND_ACCENT_SECONDARY + "on " + Colours.BRAND_ACCENT_SECONDARY + targetName + Colours.BRAND_ACCENT_SECONDARY + ".");
            if (t.isOnline() && t.getPlayer() != null) {
                msg().send(t.getPlayer(), Colours.BRAND_ACCENT_SECONDARY + "You have been given a personal slowmode of " + Colours.BRAND_ACCENT_SECONDARY + arg1 + Colours.BRAND_ACCENT_SECONDARY + ".");
            }
        } else {
            chat().set("global", millis);
            Text.broadcast(SauverMessages.screen(Messages.prefix()
                    + " " + Colours.BRAND_ACCENT_SECONDARY + "Slowmode has been set to " + Colours.BRAND_ACCENT_SECONDARY + arg1 + Colours.BRAND_ACCENT_SECONDARY + "."));
        }
    }

    public boolean slowmodeGate(Player p) {
        if (p.hasPermission(BYPASS)) {
            return false;
        }
        UUID u = p.getUniqueId();
        long sm = chat().getLong("player." + u, 0);
        if (sm <= 0) {
            sm = chat().getLong("global", 0);
        }
        if (sm > 0) {
            long last = chat().getLong("last." + u, 0);
            if (last > 0) {
                long diff = System.currentTimeMillis() - last;
                if (diff < sm) {
                    long remain = sm - diff;
                    msg().err(p, Colours.WARNING + "Your slowmode is active. " + Colours.BRAND_ACCENT_SECONDARY + "Time remaining: " + Colours.BRAND_ACCENT_SECONDARY
                            + SauverFormat.fancyTime(remain));
                    return true;
                }
            }
        }
        chat().set("last." + u, System.currentTimeMillis());
        return false;
    }

    public List<String> slowmodeTab(int pos) {
        if (pos == 1) {
            return List.of("off", "2s", "5s", "10s", "1m", "5m", "30m", "1h", "6h");
        }
        if (pos == 2) {
            return onlineNames();
        }
        return List.of();
    }

    static List<String> onlineNames() {
        List<String> out = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            out.add(p.getName());
        }
        return out;
    }
}

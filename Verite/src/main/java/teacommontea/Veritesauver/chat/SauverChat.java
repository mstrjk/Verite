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
            msg().send(sender, teacommontea.util.Lang.of("chat.broadcast.usage"));
            return;
        }
        String message = String.join(" ", args);
        teacommontea.util.text.Server.broadcast(" ");
        teacommontea.util.text.Server.broadcast(teacommontea.util.text.Text.toLegacy(Messages.prefix()
                + Colours.BRAND_ACCENT_SECONDARY + ": " + Colours.BRAND_ACCENT_SECONDARY + message));
        teacommontea.util.text.Server.broadcast(" ");
    }

    public void chatClear(CommandSender sender) {
        for (int i = 0; i < 300; i++) {
            teacommontea.util.text.Server.broadcast(" ");
        }
        String who = sender instanceof Player p ? p.getName() : SauverEngine.CONSOLE_NAME;
        Text.broadcast(SauverMessages.screen(teacommontea.util.Lang.of("chat.cleared", "who", who)));
    }

    public void chatMute(CommandSender sender) {
        boolean muted = isChatMuted();
        if (!muted) {
            chat().set("muted", true);
            Text.broadcast(SauverMessages.screen(teacommontea.util.Lang.of("chat.muted.broadcast")));
        } else {
            chat().set("muted", false);
            Text.broadcast(SauverMessages.screen(teacommontea.util.Lang.of("chat.unmuted.broadcast")));
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
            msg().err(p, teacommontea.util.Lang.of("chat.muted.deny"));
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
                Text.broadcast(SauverMessages.screen(Messages.prefix() + " " + teacommontea.util.Lang.of("slowmode.disabled")));
            } else {
                msg().send(sender, teacommontea.util.Lang.of("slowmode.usage"));
            }
            return;
        }
        if (arg1.equalsIgnoreCase("off")) {
            if (targetName != null) {
                OfflinePlayer t = Bukkit.getOfflinePlayer(targetName);
                chat().delete("player." + t.getUniqueId());
                msg().send(sender, teacommontea.util.Lang.of("slowmode.removed.other", "name", targetName));
                if (t.isOnline() && t.getPlayer() != null) {
                    msg().send(t.getPlayer(), teacommontea.util.Lang.of("slowmode.removed.self"));
                }
            } else {
                chat().delete("global");
                Text.broadcast(SauverMessages.screen(Messages.prefix() + " " + teacommontea.util.Lang.of("slowmode.disabled")));
            }
            return;
        }
        long millis = SauverDuration.parseShort(arg1);
        if (millis < 0) {
            msg().err(sender, teacommontea.util.Lang.of("slowmode.duration.invalid"));
            return;
        }
        if (millis <= 0) {
            msg().err(sender, teacommontea.util.Lang.of("slowmode.duration.too.small"));
            return;
        }
        if (millis > MAX_SLOWMODE) {
            msg().err(sender, teacommontea.util.Lang.of("slowmode.duration.too.large"));
            return;
        }
        if (targetName != null) {
            OfflinePlayer t = Bukkit.getOfflinePlayer(targetName);
            chat().set("player." + t.getUniqueId(), millis);
            msg().send(sender, teacommontea.util.Lang.of("slowmode.set.other", "duration", arg1, "name", targetName));
            if (t.isOnline() && t.getPlayer() != null) {
                msg().send(t.getPlayer(), teacommontea.util.Lang.of("slowmode.set.self", "duration", arg1));
            }
        } else {
            chat().set("global", millis);
            Text.broadcast(SauverMessages.screen(Messages.prefix() + " "
                    + teacommontea.util.Lang.of("slowmode.set.broadcast", "duration", arg1)));
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
                    msg().err(p, teacommontea.util.Lang.of("slowmode.active", "duration", SauverFormat.fancyTime(remain)));
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

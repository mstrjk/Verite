package teacommontea.veritemaison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import teacommontea.util.Lang;

final class TeleportRequests implements Listener {

    static final String ASK = "veritemaison.tpa";
    static final String ASK_HERE = "veritemaison.tpahere";
    static final String ANSWER = "veritemaison.tpaccept";

    private record Request(UUID requester, UUID target, boolean here, long expiresAt) {}

    private final Teleport teleport;
    private final long expiryMillis;
    private final Map<UUID, LinkedHashMap<UUID, Request>> byTarget = new HashMap<>();

    TeleportRequests(Teleport teleport, long expiryMillis) {
        this.teleport = teleport;
        this.expiryMillis = expiryMillis;
    }

    int ask(CommandSender sender, Entity executor, List<? extends Entity> named, String label, boolean here) {
        if (!(executor instanceof Player requester)) {
            return teleport.playersOnly(sender, label);
        }
        Player target = teleport.onePlayer(sender, named);
        if (target == null) {
            return 0;
        }
        if (target.equals(requester)) {
            teleport.send(sender, Lang.of("maison.tpa.self"));
            return 0;
        }
        synchronized (byTarget) {
            LinkedHashMap<UUID, Request> pending = byTarget.computeIfAbsent(target.getUniqueId(), k -> new LinkedHashMap<>());
            pending.remove(requester.getUniqueId());
            pending.put(requester.getUniqueId(), new Request(requester.getUniqueId(), target.getUniqueId(), here,
                    System.currentTimeMillis() + expiryMillis));
        }
        String seconds = String.valueOf(expiryMillis / 1000L);
        teleport.send(sender, Lang.of("maison.tpa.sent", "name", target.getName(), "seconds", seconds));
        teleport.send(target, Lang.of(here ? "maison.tpahere.received" : "maison.tpa.received",
                "name", requester.getName(), "seconds", seconds));
        return 1;
    }

    int answer(CommandSender sender, Entity executor, List<? extends Entity> named, String label, boolean accept) {
        if (!(executor instanceof Player target)) {
            return teleport.playersOnly(sender, label);
        }
        UUID from = firstPlayer(named);
        if (named != null && from == null) {
            teleport.send(sender, Lang.of("maison.player.none"));
            return 0;
        }
        Request request = take(target.getUniqueId(), from);
        if (request == null) {
            if (from == null) {
                teleport.send(sender, Lang.of("maison.tpa.none"));
            } else {
                teleport.send(sender, Lang.of("maison.tpa.none.from", "name", nameOf(named)));
            }
            return 0;
        }
        Player requester = Bukkit.getPlayer(request.requester());
        if (requester == null || !requester.isOnline()) {
            teleport.send(sender, Lang.of("player.offline"));
            return 0;
        }
        if (!accept) {
            teleport.send(target, Lang.of("maison.tpa.denied", "name", requester.getName()));
            teleport.send(requester, Lang.of("maison.tpa.denied.other", "name", target.getName()));
            return 1;
        }
        teleport.send(target, Lang.of("maison.tpa.accepted", "name", requester.getName()));
        teleport.send(requester, Lang.of("maison.tpa.accepted.other", "name", target.getName()));
        if (request.here()) {
            teleport.transport(target, List.of(target), requester, true);
        } else {
            teleport.transport(requester, List.of(requester), target, true);
        }
        return 1;
    }

    int cancel(CommandSender sender, Entity executor, List<? extends Entity> named, String label) {
        if (!(executor instanceof Player requester)) {
            return teleport.playersOnly(sender, label);
        }
        UUID only = firstPlayer(named);
        if (named != null && only == null) {
            teleport.send(sender, Lang.of("maison.player.none"));
            return 0;
        }
        List<UUID> cancelled = new ArrayList<>();
        synchronized (byTarget) {
            for (Iterator<Map.Entry<UUID, LinkedHashMap<UUID, Request>>> it = byTarget.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UUID, LinkedHashMap<UUID, Request>> entry = it.next();
                if (only != null && !only.equals(entry.getKey())) {
                    continue;
                }
                Request removed = entry.getValue().remove(requester.getUniqueId());
                if (removed != null && removed.expiresAt() > System.currentTimeMillis()) {
                    cancelled.add(entry.getKey());
                }
                if (entry.getValue().isEmpty()) {
                    it.remove();
                }
            }
        }
        if (cancelled.isEmpty()) {
            teleport.send(sender, Lang.of("maison.tpa.none.outgoing"));
            return 0;
        }
        for (UUID targetId : cancelled) {
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) {
                continue;
            }
            teleport.send(sender, Lang.of("maison.tpa.cancelled", "name", target.getName()));
            teleport.send(target, Lang.of("maison.tpa.cancelled.other", "name", requester.getName()));
        }
        return cancelled.size();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID gone = event.getPlayer().getUniqueId();
        synchronized (byTarget) {
            byTarget.remove(gone);
            byTarget.values().removeIf(pending -> {
                pending.remove(gone);
                return pending.isEmpty();
            });
        }
    }

    private Request take(UUID target, UUID from) {
        long now = System.currentTimeMillis();
        synchronized (byTarget) {
            LinkedHashMap<UUID, Request> pending = byTarget.get(target);
            if (pending == null) {
                return null;
            }
            pending.values().removeIf(request -> request.expiresAt() <= now);
            Request chosen = null;
            if (from != null) {
                chosen = pending.remove(from);
            } else if (!pending.isEmpty()) {
                UUID newest = null;
                for (UUID key : pending.keySet()) {
                    newest = key;
                }
                chosen = pending.remove(newest);
            }
            if (pending.isEmpty()) {
                byTarget.remove(target);
            }
            return chosen;
        }
    }

    private static UUID firstPlayer(List<? extends Entity> named) {
        if (named == null) {
            return null;
        }
        for (Entity entity : named) {
            if (entity instanceof Player player) {
                return player.getUniqueId();
            }
        }
        return null;
    }

    private static String nameOf(List<? extends Entity> named) {
        return named == null || named.isEmpty() ? "" : named.get(0).getName();
    }
}

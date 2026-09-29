package teacommontea.veritesauver.client;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class SignProbe {

    private static final String HANDLER_NAME = "verite_client_probe";
    private static final int LINES = 4;
    private static final long ROUND_TIMEOUT_TICKS = 200L;
    private static final int PROBE_DEPTH = 4;

    private final ProbeNms nms;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    SignProbe(ProbeNms nms) {
        this.nms = nms;
    }

    private static final class Session {
        final Player player;
        final List<ClientSignatures.Probe> queue;
        final List<Signal> found = new ArrayList<>();
        final Consumer<List<Signal>> onDone;
        final int baseX;
        final int baseY;
        final int baseZ;
        int cursor;
        int round;
        teacommontea.util.sched.TaskHandle timeout;

        Session(Player player, List<ClientSignatures.Probe> queue, int[] base, Consumer<List<Signal>> onDone) {
            this.player = player;
            this.queue = queue;
            this.baseX = base[0];
            this.baseY = base[1];
            this.baseZ = base[2];
            this.onDone = onDone;
        }
    }

    void start(Player p, Consumer<List<Signal>> onDone) {
        Channel ch = nms.channelOf(p);
        if (ch == null) {
            onDone.accept(List.of());
            return;
        }
        List<ClientSignatures.Probe> queue = new ArrayList<>(ClientSignatures.KEYBINDS.size()
                + ClientSignatures.TRANSLATIONS.size());
        queue.addAll(ClientSignatures.KEYBINDS);
        queue.addAll(ClientSignatures.TRANSLATIONS);

        int[] base = probePosition(p);
        Session session = new Session(p, queue, base, onDone);
        if (sessions.putIfAbsent(p.getUniqueId(), session) != null) {
            onDone.accept(List.of());
            return;
        }
        attach(ch, p.getUniqueId());
        sendRound(session);
    }

    void cancel(UUID player) {
        Session session = sessions.remove(player);
        if (session != null) {
            detach(session.player);
            session.onDone.accept(new ArrayList<>(session.found));
        }
    }

    private void attach(Channel ch, UUID player) {
        if (ch.pipeline().get(HANDLER_NAME) != null) {
            return;
        }
        Inbound handler = new Inbound(player);
        if (ch.pipeline().get("packet_handler") != null) {
            ch.pipeline().addBefore("packet_handler", HANDLER_NAME, handler);
        } else {
            ch.pipeline().addLast(HANDLER_NAME, handler);
        }
    }

    private void detach(Player p) {
        Channel ch = nms.channelOf(p);
        if (ch == null || ch.pipeline().get(HANDLER_NAME) == null) {
            return;
        }
        try {
            ch.pipeline().remove(HANDLER_NAME);
        } catch (Throwable ignored) {
        }
    }

    private void sendRound(Session session) {
        if (session.cursor >= session.queue.size() || !session.player.isOnline()) {
            finish(session);
            return;
        }
        List<String> lines = new ArrayList<>(LINES);
        for (int i = 0; i < LINES; i++) {
            int index = session.cursor + i;
            lines.add(index < session.queue.size()
                    ? json(session.queue.get(index))
                    : "{\"text\":\"\"}");
        }
        int[] pos = position(session, session.round);
        try {
            nms.showProbeSign(session.player, pos[0], pos[1], pos[2], lines);
        } catch (Throwable t) {
            finish(session);
            return;
        }
        int expected = session.round;
        session.timeout = teacommontea.util.sched.Sched.executeFor(session.player, () -> {
            Session live = sessions.get(session.player.getUniqueId());
            if (live == session && live.round == expected) {
                finish(live);
            }
        }, ROUND_TIMEOUT_TICKS);
    }

    private static String json(ClientSignatures.Probe probe) {
        String field = probe.keybind() ? "keybind" : "translate";
        String token = probe.token().replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"" + field + "\":\"" + token + "\"}";
    }

    private static int[] probePosition(Player p) {
        org.bukkit.Location at = p.getLocation();
        int floor = p.getWorld().getMinHeight();
        int x = at.getBlockX();
        int z = at.getBlockZ();
        for (int y = at.getBlockY() - PROBE_DEPTH; y >= floor; y--) {
            if (replaceable(p.getWorld().getBlockAt(x, y, z))) {
                return new int[] { x, y, z };
            }
        }
        return new int[] { x, Math.max(floor, at.getBlockY() - PROBE_DEPTH), z };
    }

    private static boolean replaceable(org.bukkit.block.Block block) {
        if (block.getState() instanceof org.bukkit.inventory.InventoryHolder) {
            return false;
        }
        return block.getType().isAir() || block.getType().isSolid();
    }

    private static int[] position(Session session, int round) {
        return new int[] { session.baseX, session.baseY, session.baseZ };
    }

    private void finish(Session session) {
        sessions.remove(session.player.getUniqueId());
        if (session.timeout != null) {
            session.timeout.cancel();
            session.timeout = null;
        }
        detach(session.player);
        int[] pos = position(session, 0);
        teacommontea.util.sched.Sched.executeFor(session.player, () -> {
            try {
                nms.clearProbeSign(session.player, pos[0], pos[1], pos[2]);
            } catch (Throwable ignored) {
            }
        });
        session.onDone.accept(new ArrayList<>(session.found));
    }

    private void consume(Session session, String[] lines) {
        for (int i = 0; i < lines.length && session.cursor < session.queue.size(); i++, session.cursor++) {
            ClientSignatures.Probe probe = session.queue.get(session.cursor);
            String resolved = lines[i] == null ? "" : lines[i].trim();
            if (resolved.isEmpty() || resolved.equals(probe.token())) {
                continue;
            }
            session.found.add(Signal.of(Signal.Source.SIGN, probe.owner(), resolved, probe.weight()));
        }
        session.round++;
        if (session.timeout != null) {
            session.timeout.cancel();
            session.timeout = null;
        }
    }

    private final class Inbound extends ChannelInboundHandlerAdapter {
        private final UUID player;

        Inbound(UUID player) {
            this.player = player;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Session session = sessions.get(player);
            if (session == null || !nms.isSignUpdate(msg)) {
                super.channelRead(ctx, msg);
                return;
            }
            int[] pos = nms.positionOf(msg);
            int[] want = position(session, session.round);
            if (pos == null || pos[0] != want[0] || pos[1] != want[1] || pos[2] != want[2]) {
                super.channelRead(ctx, msg);
                return;
            }
            String[] lines = nms.linesOf(msg);
            if (lines == null) {
                super.channelRead(ctx, msg);
                return;
            }
            consume(session, lines);
            teacommontea.util.sched.Sched.executeFor(session.player, () -> {
                Session live = sessions.get(player);
                if (live != null) {
                    sendRound(live);
                }
            });
        }
    }
}

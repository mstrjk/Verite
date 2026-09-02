package teacommontea.veritevoiler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;


public final class VanishPacketInterceptor implements Listener {

    private static final String HANDLER_NAME = "verite_vanish_muffler";
    private static final double RADIUS_SQ = 4.0D * 4.0D;
    private static final long REFRESH_TICKS = 4L;

    private final Plugin plugin;
    private final Vanish vanish;
    private final VanishPacketAccess access;
    private final boolean muffleSounds;
    private final boolean muffleParticles;

    private volatile Hidden[] hidden = new Hidden[0];
    private final ConcurrentHashMap<UUID, MufflerHandler> handlers = new ConcurrentHashMap<>();
    private teacommontea.util.sched.TaskHandle task;

    private VanishPacketInterceptor(Plugin plugin, Vanish vanish, VanishPacketAccess access,
                                    boolean muffleSounds, boolean muffleParticles) {
        this.plugin = plugin;
        this.vanish = vanish;
        this.access = access;
        this.muffleSounds = muffleSounds;
        this.muffleParticles = muffleParticles;
    }

    static VanishPacketInterceptor install(Plugin plugin, Vanish vanish, VanishSettings s) {
        if (!s.muffleSounds && !s.muffleParticles) {
            return null;
        }
        VanishPacketAccess access;
        try {
            access = VanishPacketAccess.resolve();
        } catch (VanishPacketAccess.Unsupported u) {
            plugin.getLogger().warning("vanish sound/particle muffler unavailable ("
                    + u.getMessage() + "); vanish still hides the player, only ambient sounds and particles leak.");
            return null;
        }
        VanishPacketInterceptor mi = new VanishPacketInterceptor(plugin, vanish, access,
                s.muffleSounds, s.muffleParticles);
        Bukkit.getPluginManager().registerEvents(mi, plugin);
        mi.task = teacommontea.util.sched.Sched.executeGlobalRepeating(mi::refresh, REFRESH_TICKS, REFRESH_TICKS);
        for (Player p : Bukkit.getOnlinePlayers()) {
            mi.inject(p);
        }
        return mi;
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            eject(p);
        }
        hidden = new Hidden[0];
    }

    private void refresh() {
        List<Hidden> snap = new ArrayList<>();
        for (UUID id : vanish.getVanished()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            Location loc = p.getLocation();
            World w = loc.getWorld();
            if (w == null) continue;
            snap.add(new Hidden(id, w.getUID(), loc.getX(), loc.getY(), loc.getZ(), Vanish.tier(p)));
        }
        hidden = snap.toArray(new Hidden[0]);

        for (MufflerHandler h : handlers.values()) {
            Player viewer = Bukkit.getPlayer(h.viewerId);
            if (viewer == null || !viewer.isOnline()) {
                h.canSee = false;
                h.viewerWorld = null;
                continue;
            }
            h.canSee = viewer.hasPermission("verite.vanish.see");
            h.viewerTier = Vanish.tier(viewer);
            World vw = viewer.getWorld();
            h.viewerWorld = vw == null ? null : vw.getUID();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        inject(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        eject(e.getPlayer());
    }

    private void inject(Player p) {
        try {
            Channel ch = access.channelOf(p);
            if (ch == null || ch.pipeline().get(HANDLER_NAME) != null) {
                return;
            }
            var pipeline = ch.pipeline();
            MufflerHandler handler = new MufflerHandler(p.getUniqueId());

            if (pipeline.get("packet_handler") != null) {
                pipeline.addBefore("packet_handler", HANDLER_NAME, handler);
            } else if (pipeline.get("encoder") != null) {
                pipeline.addAfter("encoder", HANDLER_NAME, handler);
            } else {
                pipeline.addLast(HANDLER_NAME, handler);
            }
            handlers.put(p.getUniqueId(), handler);
        } catch (Throwable t) {
            plugin.getLogger().warning("could not inject vanish muffler for " + p.getName() + ": " + t);
        }
    }

    private void eject(Player p) {
        handlers.remove(p.getUniqueId());
        try {
            Channel ch = access.channelOf(p);
            if (ch != null && ch.pipeline().get(HANDLER_NAME) != null) {
                ch.pipeline().remove(HANDLER_NAME);
            }
        } catch (Throwable ignored) {
        }
    }

    private final class MufflerHandler extends ChannelOutboundHandlerAdapter {
        private final UUID viewerId;
        private volatile boolean canSee;
        private volatile int viewerTier;
        private volatile UUID viewerWorld;

        MufflerHandler(UUID viewerId) {
            this.viewerId = viewerId;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            try {
                if (shouldDrop(msg)) {
                    promise.setSuccess();
                    return;
                }
            } catch (Throwable ignored) {
            }
            super.write(ctx, msg, promise);
        }

        private boolean shouldDrop(Object packet) {
            Hidden[] snap = hidden;
            if (snap.length == 0) return false;
            boolean sound = muffleSounds && access.isSound(packet);
            boolean particle = !sound && muffleParticles && access.isParticle(packet);
            if (!sound && !particle) return false;
            double[] pos = access.positionOf(packet);
            if (pos == null) return false;
            UUID world = viewerWorld;
            boolean viewerCanSee = canSee;
            int tier = viewerTier;
            for (Hidden h : snap) {
                if (h.player.equals(viewerId)) continue;
                if (world != null && !world.equals(h.world)) continue;
                double dx = pos[0] - h.x;
                double dy = pos[1] - h.y;
                double dz = pos[2] - h.z;
                if (dx * dx + dy * dy + dz * dz > RADIUS_SQ) continue;
                boolean viewerSeesThisHidden = viewerCanSee && tier >= h.tier;
                if (!viewerSeesThisHidden) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class Hidden {
        final UUID player;
        final UUID world;
        final double x;
        final double y;
        final double z;
        final int tier;
        Hidden(UUID player, UUID world, double x, double y, double z, int tier) {
            this.player = player;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.tier = tier;
        }
    }
}

package teacommontea.veritesauver.client;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

final class KnownPackProbe {

    private static final String OUT_NAME = "verite_pack_probe_out";
    private static final String IN_NAME = "verite_pack_probe_in";
    private static final String PROBE_VERSION = "1";
    private static final long SCAN_TICKS = 5L;
    private static final long ATTACH_DELAY_MS = 0L;

    private final Class<?> clientboundClass;
    private final Class<?> serverboundClass;
    private final Constructor<?> clientboundCtor;
    private final Constructor<?> serverboundCtor;
    private final Constructor<?> knownPackCtor;
    private final Method clientboundPacks;
    private final Method serverboundPacks;
    private final Method packNamespace;
    private final Method packId;
    private final List<Object> connections;
    private final Field connectionChannel;

    private final Map<SocketAddress, List<Signal>> results = new ConcurrentHashMap<>();
    private teacommontea.util.sched.TaskHandle scanTask;
    private List<Object> watchedList;
    private Field watchedField;
    private Object watchedOwner;

    private KnownPackProbe(Class<?> clientboundClass, Class<?> serverboundClass,
                           Constructor<?> clientboundCtor, Constructor<?> serverboundCtor,
                           Constructor<?> knownPackCtor, Method clientboundPacks,
                           Method serverboundPacks, Method packNamespace, Method packId,
                           List<Object> connections, Field connectionChannel) {
        this.clientboundClass = clientboundClass;
        this.serverboundClass = serverboundClass;
        this.clientboundCtor = clientboundCtor;
        this.serverboundCtor = serverboundCtor;
        this.knownPackCtor = knownPackCtor;
        this.clientboundPacks = clientboundPacks;
        this.serverboundPacks = serverboundPacks;
        this.packNamespace = packNamespace;
        this.packId = packId;
        this.connections = connections;
        this.connectionChannel = connectionChannel;
    }

    static KnownPackProbe install() {
        try {
            Class<?> clientbound = Class.forName(
                    "net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks");
            Class<?> serverbound = Class.forName(
                    "net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks");
            Class<?> knownPack = Class.forName("net.minecraft.server.packs.repository.KnownPack");

            Method cbPacks = component(clientbound, List.class, 0);
            Method sbPacks = component(serverbound, List.class, 0);
            Method namespace = component(knownPack, String.class, 0);
            Method id = component(knownPack, String.class, 1);
            if (cbPacks == null || sbPacks == null || namespace == null || id == null) {
                return null;
            }

            Class<?> connectionClass = firstClass("net.minecraft.network.Connection",
                    "net.minecraft.network.NetworkManager");
            if (connectionClass == null) {
                return null;
            }
            Field channel = teacommontea.util.NmsFields.firstFieldAssignableTo(connectionClass, Channel.class);
            if (channel == null) {
                return null;
            }
            channel.setAccessible(true);

            Holder holder = connectionList(connectionClass);
            if (holder == null) {
                return null;
            }

            KnownPackProbe probe = new KnownPackProbe(clientbound, serverbound,
                    clientbound.getConstructor(List.class), serverbound.getConstructor(List.class),
                    knownPack.getConstructor(String.class, String.class, String.class),
                    cbPacks, sbPacks, namespace, id, holder.list, channel);
            if (probe.watchConnections(holder)) {
            } else {
                probe.scanTask = teacommontea.util.sched.Sched.executeGlobalRepeating(
                        probe::attachNewConnections, 1L, 1L);
            }
            return probe;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Method component(Class<?> owner, Class<?> type, int index) {
        RecordComponent[] parts = owner.getRecordComponents();
        if (parts == null) {
            return null;
        }
        int seen = 0;
        for (RecordComponent rc : parts) {
            if (rc.getType() != type) continue;
            if (seen++ != index) continue;
            Method m = rc.getAccessor();
            m.setAccessible(true);
            return m;
        }
        return null;
    }

    private record Holder(List<Object> list, Field field, Object owner) {}

    @SuppressWarnings("unchecked")
    private boolean watchConnections(Holder holder) {
        try {
            List<Object> live = holder.list();
            List<Object> watched = java.util.Collections.synchronizedList(new java.util.ArrayList<>(live) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean add(Object element) {
                    boolean added = super.add(element);
                    if (added) {
                        hook(element);
                    }
                    return added;
                }
            });
            holder.field().set(holder.owner(), watched);
            this.watchedList = watched;
            this.watchedField = holder.field();
            this.watchedOwner = holder.owner();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private void hook(Object connection) {
        try {
            Object value = connectionChannel.get(connection);
            if (!(value instanceof Channel ch)) {
                return;
            }
            ch.eventLoop().execute(() -> attach(ch));
        } catch (Throwable ignored) {
        }
    }

    private void attach(Channel ch) {
        try {
            if (!ch.isActive() || ch.pipeline().get(OUT_NAME) != null) {
                return;
            }
            if (ch.pipeline().get("packet_handler") == null) {
                return;
            }
            ch.pipeline().addBefore("packet_handler", OUT_NAME, new Negotiation(ch));
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static Holder connectionList(Class<?> connectionClass) throws Throwable {
        Object server = Bukkit.getServer();
        Method getServer = server.getClass().getMethod("getServer");
        getServer.setAccessible(true);
        Object nmsServer = getServer.invoke(server);

        Class<?> listenerClass = firstClass("net.minecraft.server.network.ServerConnectionListener",
                "net.minecraft.server.network.ServerConnection");
        if (listenerClass == null) {
            return null;
        }
        Object listener = null;
        for (Class<?> c = nmsServer.getClass(); c != null && c != Object.class && listener == null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 0 || !listenerClass.isAssignableFrom(m.getReturnType())) continue;
                m.setAccessible(true);
                Object value = m.invoke(nmsServer);
                if (value != null) {
                    listener = value;
                    break;
                }
            }
        }
        if (listener == null) {
            return null;
        }
        for (Field f : listenerClass.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || !List.class.isAssignableFrom(f.getType())) continue;
            if (!elementType(f, connectionClass)) continue;
            f.setAccessible(true);
            if (f.get(listener) instanceof List<?> list) {
                return new Holder((List<Object>) list, f, listener);
            }
        }
        return null;
    }

    private static boolean elementType(Field field, Class<?> wanted) {
        return field.getGenericType() instanceof java.lang.reflect.ParameterizedType pt
                && pt.getActualTypeArguments().length == 1
                && pt.getActualTypeArguments()[0] == wanted;
    }

    private void attachNewConnections() {
        List<?> snapshot;
        try {
            snapshot = new ArrayList<>(connections);
        } catch (Throwable t) {
            return;
        }
        for (Object connection : snapshot) {
            try {
                Object value = connectionChannel.get(connection);
                if (!(value instanceof Channel ch) || !ch.isActive()) continue;
                if (ch.pipeline().get(OUT_NAME) != null) continue;
                if (ch.pipeline().get("packet_handler") == null) continue;
                ch.pipeline().addBefore("packet_handler", OUT_NAME, new Negotiation(ch));
            } catch (Throwable ignored) {
            }
        }
    }

    private static Class<?> firstClass(String... names) {
        for (String n : names) {
            try {
                return Class.forName(n);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    List<Signal> claim(SocketAddress address) {
        List<Signal> found = results.remove(address);
        return found == null ? List.of() : found;
    }

    void forget(SocketAddress address) {
        results.remove(address);
    }

    void shutdown() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        if (watchedField != null && watchedOwner != null && watchedList != null) {
            try {
                watchedField.set(watchedOwner,
                        java.util.Collections.synchronizedList(new java.util.ArrayList<>(watchedList)));
            } catch (Throwable ignored) {
            }
            watchedField = null;
            watchedOwner = null;
            watchedList = null;
        }
        for (Object connection : new ArrayList<>(connections)) {
            try {
                Object value = connectionChannel.get(connection);
                if (!(value instanceof Channel ch)) continue;
                removeHandler(ch, OUT_NAME);
                removeHandler(ch, IN_NAME);
            } catch (Throwable ignored) {
            }
        }
        results.clear();
    }

    private static void removeHandler(Channel ch, String name) {
        try {
            if (ch.pipeline().get(name) != null) {
                ch.pipeline().remove(name);
            }
        } catch (Throwable ignored) {
        }
    }

    private final class Negotiation extends ChannelOutboundHandlerAdapter {
        private final Channel channel;
        private volatile List<Object> injected;

        Negotiation(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (injected == null && clientboundClass.isInstance(msg)) {
                Object rewritten = inject(msg);
                if (rewritten != null) {
                    if (ctx.pipeline().get(IN_NAME) == null) {
                        ctx.pipeline().addBefore(OUT_NAME, IN_NAME, new Response(this));
                    }
                    super.write(ctx, rewritten, promise);
                    return;
                }
            }
            super.write(ctx, msg, promise);
        }

        private Object inject(Object packet) {
            try {
                List<?> real = (List<?>) clientboundPacks.invoke(packet);
                if (real == null) {
                    return null;
                }
                List<Object> all = new ArrayList<>(real);
                List<Object> probes = new ArrayList<>();
                for (ClientSignatures.Probe probe : ClientSignatures.KNOWN_PACKS) {
                    String token = probe.token();
                    int colon = token.indexOf(':');
                    if (colon <= 0) continue;
                    Object pack = knownPackCtor.newInstance(token.substring(0, colon),
                            token.substring(colon + 1), PROBE_VERSION);
                    probes.add(pack);
                    all.add(pack);
                }
                if (probes.isEmpty()) {
                    return null;
                }
                injected = probes;
                return clientboundCtor.newInstance(List.copyOf(all));
            } catch (Throwable t) {
                return null;
            }
        }

        Object filter(Object response) {
            List<Object> probes = injected;
            if (probes == null) {
                return response;
            }
            try {
                List<?> echoed = (List<?>) serverboundPacks.invoke(response);
                if (echoed == null) {
                    return response;
                }
                List<Object> kept = new ArrayList<>(echoed.size());
                List<Signal> found = new ArrayList<>();
                for (Object pack : echoed) {
                    if (!probes.contains(pack)) {
                        kept.add(pack);
                        continue;
                    }
                    String token = packNamespace.invoke(pack) + ":" + packId.invoke(pack);
                    for (ClientSignatures.Probe probe : ClientSignatures.KNOWN_PACKS) {
                        if (probe.token().equals(token)) {
                            found.add(Signal.of(Signal.Source.KNOWN_PACK, probe.owner(), token, probe.weight()));
                            break;
                        }
                    }
                }
                if (!found.isEmpty() && channel.remoteAddress() != null) {
                    results.put(channel.remoteAddress(), found);
                }
                return serverboundCtor.newInstance(List.copyOf(kept));
            } catch (Throwable t) {
                return response;
            }
        }
    }

    private final class Response extends ChannelInboundHandlerAdapter {
        private final Negotiation owner;

        Response(Negotiation owner) {
            this.owner = owner;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!serverboundClass.isInstance(msg)) {
                super.channelRead(ctx, msg);
                return;
            }
            Object filtered = owner.filter(msg);
            ctx.pipeline().remove(this);
            super.channelRead(ctx, filtered);
        }
    }
}

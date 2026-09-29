package teacommontea.veritemaison;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import teacommontea.util.ConsoleColours;
import teacommontea.util.Lang;
import teacommontea.util.Trace;
import teacommontea.util.sched.Sched;

final class SpigotCommandTree implements Listener {

    enum Shape { TELEPORT, PLAYER, OPTIONAL_PLAYER }

    private record Entry(org.bukkit.command.Command command, Shape shape, List<String> labels) {}

    private record Types(ArgumentType<?> location, ArgumentType<?> rotation,
                         ArgumentType<?> destination, ArgumentType<?> targets, ArgumentType<?> player) {}

    private final Plugin plugin;
    private final SimpleCommandMap map;
    private final String prefix;
    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, WeakReference<CommandNode<?>>> grafted = new HashMap<>();

    private Method liveCommands;
    private Method dispatcherOf;
    private Method removeCommand;
    private Object server;
    private Field vanillaField;
    private boolean broken;

    SpigotCommandTree(Plugin plugin, SimpleCommandMap map, String prefix) {
        this.plugin = plugin;
        this.map = map;
        this.prefix = prefix;
    }

    void add(org.bukkit.command.Command command, Shape shape) {
        List<String> labels = new ArrayList<>();
        labels.add(command.getName());
        labels.add(prefix + ":" + command.getName());
        for (String alias : command.getAliases()) {
            labels.add(alias);
            labels.add(prefix + ":" + alias);
        }
        entries.add(new Entry(command, shape, labels));
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (ensure()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Sched.executeFor(player, player::updateCommands);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (ensure()) {
            Sched.executeFor(player, player::updateCommands, 1L);
        }
    }

    private boolean ensure() {
        if (broken) {
            return false;
        }
        try {
            CommandDispatcher<?> live = dispatcher(live());
            if (live == null || current(live)) {
                return false;
            }
            CommandDispatcher<?> vanilla = dispatcher(vanillaField.get(server));
            return graft(live, types(vanilla));
        } catch (ReflectiveOperationException | RuntimeException e) {
            broken = true;
            plugin.getLogger().warning(ConsoleColours.bad(Lang.of("maison.tree.failed")) + Trace.of(e));
            return false;
        }
    }

    private boolean current(CommandDispatcher<?> live) {
        for (Entry entry : entries) {
            String label = entry.command().getName();
            CommandNode<?> node = live.getRoot().getChild(label);
            WeakReference<CommandNode<?>> mine = grafted.get(label);
            if (node != null && (mine == null || node != mine.get()) && map.getKnownCommands().get(label) == entry.command()) {
                return false;
            }
        }
        return true;
    }

    private Object live() throws ReflectiveOperationException {
        if (server == null) {
            resolve();
        }
        return liveCommands.invoke(server);
    }

    private CommandDispatcher<?> dispatcher(Object commands) throws ReflectiveOperationException {
        if (commands == null) {
            return null;
        }
        return (CommandDispatcher<?>) dispatcherOf.invoke(commands);
    }

    private void resolve() throws ReflectiveOperationException {
        Object craftServer = Bukkit.getServer();
        server = craftServer.getClass().getMethod("getServer").invoke(craftServer);
        vanillaField = server.getClass().getField("vanillaCommandDispatcher");
        Class<?> commandsType = vanillaField.getType();
        liveCommands = only(server.getClass().getMethods(), m -> m.getReturnType() == commandsType);
        dispatcherOf = only(commandsType.getMethods(), m -> CommandDispatcher.class.isAssignableFrom(m.getReturnType()));
        removeCommand = CommandNode.class.getMethod("removeCommand", String.class);
    }

    private static Method only(Method[] methods, Predicate<Method> shape) throws NoSuchMethodException {
        Method found = null;
        for (Method m : methods) {
            if (m.getParameterCount() != 0 || !shape.test(m)) {
                continue;
            }
            if (found != null && !found.getName().equals(m.getName())) {
                throw new NoSuchMethodException("ambiguous " + found + " and " + m);
            }
            found = m;
        }
        if (found == null) {
            throw new NoSuchMethodException("no accessor of the expected shape");
        }
        return found;
    }

    private static Types types(CommandDispatcher<?> vanilla) throws NoSuchFieldException {
        CommandNode<?> teleport = child(vanilla.getRoot(), "teleport");
        CommandNode<?> targets = child(teleport, "targets");
        CommandNode<?> spectate = vanilla.getRoot().getChild("spectate");
        CommandNode<?> spectatePlayer = spectate == null || spectate.getChild("target") == null
                ? null : spectate.getChild("target").getChild("player");
        ArgumentType<?> destination = type(child(teleport, "destination"));
        return new Types(
                type(child(teleport, "location")),
                type(child(child(targets, "location"), "rotation")),
                destination,
                type(targets),
                spectatePlayer == null ? destination : type(spectatePlayer));
    }

    private static CommandNode<?> child(CommandNode<?> parent, String name) throws NoSuchFieldException {
        CommandNode<?> node = parent.getChild(name);
        if (node == null) {
            throw new NoSuchFieldException("vanilla node " + name);
        }
        return node;
    }

    private static ArgumentType<?> type(CommandNode<?> node) throws NoSuchFieldException {
        if (node instanceof ArgumentCommandNode<?, ?> argument) {
            return argument.getType();
        }
        throw new NoSuchFieldException("vanilla node " + node.getName() + " is not an argument");
    }

    private <S> boolean graft(CommandDispatcher<S> live, Types types) throws ReflectiveOperationException {
        boolean changed = false;
        for (Entry entry : entries) {
            for (String label : entry.labels()) {
                if (map.getKnownCommands().get(label) != entry.command()) {
                    continue;
                }
                CommandNode<S> existing = live.getRoot().getChild(label);
                if (existing == null) {
                    continue;
                }
                LiteralCommandNode<S> node = entry.shape() == Shape.TELEPORT
                        ? teleport(label, existing.getRequirement(), existing.getCommand(), types)
                        : player(label, existing.getRequirement(), existing.getCommand(), types,
                                entry.shape() == Shape.OPTIONAL_PLAYER);
                removeCommand.invoke(live.getRoot(), label);
                live.getRoot().addChild(node);
                if (label.equals(entry.command().getName())) {
                    grafted.put(label, new WeakReference<>(node));
                }
                changed = true;
            }
        }
        return changed;
    }

    private static <S> LiteralCommandNode<S> teleport(String label, Predicate<S> requirement, Command<S> run, Types types) {
        return LiteralArgumentBuilder.<S>literal(label)
                .requires(requirement)
                .then(SpigotCommandTree.<S>argument("location", types.location()).executes(run)
                        .then(SpigotCommandTree.<S>argument("rotation", types.rotation()).executes(run)))
                .then(SpigotCommandTree.<S>argument("destination", types.destination()).executes(run))
                .then(SpigotCommandTree.<S>argument("targets", types.targets())
                        .then(SpigotCommandTree.<S>argument("location", types.location()).executes(run)
                                .then(SpigotCommandTree.<S>argument("rotation", types.rotation()).executes(run)))
                        .then(SpigotCommandTree.<S>argument("destination", types.destination()).executes(run)))
                .build();
    }

    private static <S> LiteralCommandNode<S> player(String label, Predicate<S> requirement, Command<S> run,
                                                    Types types, boolean optional) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.<S>literal(label).requires(requirement);
        if (optional) {
            root.executes(run);
        }
        return root.then(SpigotCommandTree.<S>argument("player", types.player()).executes(run)).build();
    }

    private static <S> RequiredArgumentBuilder<S, ?> argument(String name, ArgumentType<?> type) {
        return captured(name, type);
    }

    private static <S, T> RequiredArgumentBuilder<S, T> captured(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}

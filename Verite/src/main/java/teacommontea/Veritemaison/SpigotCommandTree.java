package teacommontea.veritemaison;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

    private record Types(ArgumentType<?> location, ArgumentType<?> rotation,
                         ArgumentType<?> destination, ArgumentType<?> targets) {}

    private final Plugin plugin;
    private final SimpleCommandMap map;
    private final org.bukkit.command.Command command;
    private final List<String> labels = new ArrayList<>();

    private Method liveCommands;
    private Method dispatcherOf;
    private Method removeCommand;
    private Object server;
    private Field vanillaField;
    private boolean broken;
    private WeakReference<CommandNode<?>> grafted = new WeakReference<>(null);

    SpigotCommandTree(Plugin plugin, SimpleCommandMap map, org.bukkit.command.Command command, String prefix) {
        this.plugin = plugin;
        this.map = map;
        this.command = command;
        labels.add(Teleport.LABEL);
        labels.add(prefix + ":" + Teleport.LABEL);
        for (String alias : Teleport.ALIASES) {
            labels.add(alias);
            labels.add(prefix + ":" + alias);
        }
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
            if (live == null) {
                return false;
            }
            CommandNode<?> current = live.getRoot().getChild(Teleport.LABEL);
            if (current != null && current == grafted.get()) {
                return false;
            }
            CommandDispatcher<?> vanilla = dispatcher(vanillaField.get(server));
            return graft(live, types(vanilla));
        } catch (ReflectiveOperationException | RuntimeException e) {
            broken = true;
            plugin.getLogger().warning(ConsoleColours.bad(Lang.of("maison.teleport.tree.failed")) + Trace.of(e));
            return false;
        }
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
        return new Types(
                type(child(teleport, "location")),
                type(child(child(targets, "location"), "rotation")),
                type(child(teleport, "destination")),
                type(targets));
    }

    private static CommandNode<?> child(CommandNode<?> parent, String name) throws NoSuchFieldException {
        CommandNode<?> node = parent.getChild(name);
        if (node == null) {
            throw new NoSuchFieldException("vanilla teleport node " + name);
        }
        return node;
    }

    private static ArgumentType<?> type(CommandNode<?> node) throws NoSuchFieldException {
        if (node instanceof ArgumentCommandNode<?, ?> argument) {
            return argument.getType();
        }
        throw new NoSuchFieldException("vanilla teleport node " + node.getName() + " is not an argument");
    }

    private <S> boolean graft(CommandDispatcher<S> live, Types types) throws ReflectiveOperationException {
        boolean changed = false;
        for (String label : labels) {
            if (map.getKnownCommands().get(label) != command) {
                continue;
            }
            CommandNode<S> existing = live.getRoot().getChild(label);
            if (existing == null) {
                continue;
            }
            LiteralCommandNode<S> node = typed(label, existing.getRequirement(), existing.getCommand(), types);
            removeCommand.invoke(live.getRoot(), label);
            live.getRoot().addChild(node);
            if (label.equals(Teleport.LABEL)) {
                grafted = new WeakReference<>(node);
            }
            changed = true;
        }
        return changed;
    }

    private static <S> LiteralCommandNode<S> typed(String label, Predicate<S> requirement, Command<S> run, Types types) {
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

    private static <S> RequiredArgumentBuilder<S, ?> argument(String name, ArgumentType<?> type) {
        return captured(name, type);
    }

    private static <S, T> RequiredArgumentBuilder<S, T> captured(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}

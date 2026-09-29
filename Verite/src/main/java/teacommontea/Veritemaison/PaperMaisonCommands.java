package teacommontea.veritemaison;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Rotation;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import teacommontea.util.Lang;
import teacommontea.util.sched.Sched;

public final class PaperMaisonCommands {

    private PaperMaisonCommands() {}

    public static void register(JavaPlugin plugin, Set<Maison.Feature> features, long requestExpiryMillis) {
        Teleport teleport = new Teleport(PaperMaisonCommands::move);
        List<Maison.PlayerCommand> playerCommands = Maison.playerCommands(plugin, features, teleport, requestExpiryMillis);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            if (features.contains(Maison.Feature.TELEPORT)) {
                registrar.register(teleportTree(teleport), Lang.of("maison.teleport.description"), Teleport.ALIASES);
            }
            for (Maison.PlayerCommand command : playerCommands) {
                registrar.register(playerTree(command), command.description(), List.of());
            }
        });
    }

    private static CompletableFuture<Boolean> move(Entity entity, Location to) {
        CompletableFuture<Boolean> done = new CompletableFuture<>();
        Runnable go = () -> entity.teleportAsync(to, TeleportCause.COMMAND, TeleportFlag.EntityState.RETAIN_PASSENGERS)
                .whenComplete((moved, error) -> done.complete(error == null && Boolean.TRUE.equals(moved)));
        if (Sched.regionised()) {
            Sched.executeFor(entity, go);
        } else {
            go.run();
        }
        return done;
    }

    private static LiteralCommandNode<CommandSourceStack> playerTree(Maison.PlayerCommand command) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(command.label())
                .requires(source -> command.permitted(source.getSender()));
        if (command.optional()) {
            root.executes(c -> command.action().run(c.getSource().getSender(), c.getSource().getExecutor(), null, label(c)));
        }
        return root.then(Commands.argument("player", ArgumentTypes.player())
                        .executes(c -> {
                            CommandSourceStack source = c.getSource();
                            List<? extends Entity> named = c.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source);
                            return command.action().run(source.getSender(), source.getExecutor(), named, label(c));
                        }))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> teleportTree(Teleport teleport) {
        return Commands.literal(Teleport.LABEL)
                .requires(source -> Teleport.permitted(source.getSender()))
                .then(Commands.argument("location", ArgumentTypes.finePosition(true))
                        .executes(c -> toPosition(teleport, c, self(c), false))
                        .then(Commands.argument("rotation", ArgumentTypes.rotation())
                                .executes(c -> toPosition(teleport, c, self(c), true))))
                .then(Commands.argument("destination", ArgumentTypes.entity())
                        .executes(c -> toEntity(teleport, c, self(c))))
                .then(Commands.argument("targets", ArgumentTypes.entities())
                        .then(Commands.argument("location", ArgumentTypes.finePosition(true))
                                .executes(c -> toPosition(teleport, c, targets(c), false))
                                .then(Commands.argument("rotation", ArgumentTypes.rotation())
                                        .executes(c -> toPosition(teleport, c, targets(c), true))))
                        .then(Commands.argument("destination", ArgumentTypes.entity())
                                .executes(c -> toEntity(teleport, c, targets(c)))))
                .build();
    }

    private static List<? extends Entity> self(CommandContext<CommandSourceStack> c) {
        Entity executor = c.getSource().getExecutor();
        return executor == null ? null : List.of(executor);
    }

    private static List<? extends Entity> targets(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        return c.getArgument("targets", EntitySelectorArgumentResolver.class).resolve(c.getSource());
    }

    private static int toEntity(Teleport teleport, CommandContext<CommandSourceStack> c,
                                List<? extends Entity> targets) throws CommandSyntaxException {
        CommandSourceStack source = c.getSource();
        if (targets == null) {
            return teleport.consoleMustName(source.getSender(), label(c));
        }
        List<Entity> destination = c.getArgument("destination", EntitySelectorArgumentResolver.class).resolve(source);
        return teleport.toEntity(source.getSender(), source.getExecutor(), targets, destination);
    }

    private static int toPosition(Teleport teleport, CommandContext<CommandSourceStack> c,
                                  List<? extends Entity> targets, boolean rotated) throws CommandSyntaxException {
        CommandSourceStack source = c.getSource();
        if (targets == null) {
            return teleport.consoleMustName(source.getSender(), label(c));
        }
        FinePosition at = c.getArgument("location", FinePositionResolver.class).resolve(source);
        Float yaw = null, pitch = null;
        if (rotated) {
            Rotation rotation = c.getArgument("rotation", RotationResolver.class).resolve(source);
            yaw = rotation.yaw();
            pitch = rotation.pitch();
        }
        return teleport.toPosition(source.getSender(), source.getExecutor(), targets,
                source.getLocation().getWorld(), at.x(), at.y(), at.z(), yaw, pitch);
    }

    private static String label(CommandContext<CommandSourceStack> c) {
        String input = c.getInput().trim();
        if (input.startsWith("/")) {
            input = input.substring(1);
        }
        int space = input.indexOf(' ');
        return space < 0 ? input : input.substring(0, space);
    }
}

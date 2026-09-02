package teacommontea.veritesauver.invsee;

import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public final class OfflineInventories {

    public enum Kind { MAIN, ENDER }

    public sealed interface Loaded permits Loaded.Ok, Loaded.Unknown {
        record Ok(ContainerHandler view, Object human) implements Loaded {}
        record Unknown() implements Loaded {}
    }

    private final Plugin plugin;
    private final InvSeeAccess access;

    public OfflineInventories(Plugin plugin, InvSeeAccess access) {
        this.plugin = plugin;
        this.access = access;
    }

    public Loaded load(UUID uuid, String name, boolean unknownSupported, Kind kind) {
        try {
            InvSeePlayers players = new InvSeePlayers(access);
            Object world = access.mainWorldHandle();
            Object human = players.newHuman(world, uuid, name);

            Object registry = access.registryAccessOf(human);
            Optional<Object> data = access.saves().read(uuid, name, registry, human);
            if (data.isEmpty()) {
                if (!unknownSupported) {
                    return new Loaded.Unknown();
                }
            } else {
                access.readSaveData(human, data.get());
            }

            ContainerHandler view = kind == Kind.MAIN
                    ? new MainViewContainer(access, human)
                    : new EnderViewContainer(access, human);
            return new Loaded.Ok(view, human);
        } catch (Throwable t) {
            return new Loaded.Unknown();
        }
    }

    public void save(UUID uuid, String name, Kind kind, List<Object> newContents) {
        if (org.bukkit.Bukkit.getPlayer(uuid) != null) {
            return;
        }
        try {
            InvSeePlayers players = new InvSeePlayers(access);
            Object server = access.rawServer();
            Object world = access.mainWorldHandle();
            Object entity = players.newServerPlayer(server, world, uuid, name);

            Object registry = access.registryAccessOf(entity);
            Optional<Object> existing = access.saves().read(uuid, name, registry, entity);
            if (existing.isEmpty()) {
                return;
            }
            access.readSaveData(entity, existing.get());

            ContainerHandler current = kind == Kind.MAIN
                    ? new MainViewContainer(access, entity)
                    : new EnderViewContainer(access, entity);
            copyInto(current, newContents);

            access.saves().save(entity);
        } catch (Throwable ignored) {
        }
    }

    private static void copyInto(ContainerHandler container, List<Object> contents) {
        int size = Math.min(container.getContainerSize(), contents.size());
        for (int i = 0; i < size; i++) {
            container.setItem(i, contents.get(i));
        }
    }
}

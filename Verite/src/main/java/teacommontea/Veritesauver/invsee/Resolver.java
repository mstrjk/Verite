package teacommontea.veritesauver.invsee;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public final class Resolver {

    private final Plugin plugin;
    private final InvSeeAccess access;
    private final boolean onlineMode;
    private final Map<String, UUID> nameToUuid = new LinkedHashMap<>();
    private final Map<UUID, String> uuidToName = new LinkedHashMap<>();

    public Resolver(Plugin plugin, InvSeeAccess access) {
        this.plugin = plugin;
        this.access = access;
        this.onlineMode = Bukkit.getOnlineMode();
    }

    public synchronized void remember(UUID uuid, String name) {
        if (uuid == null || name == null) {
            return;
        }
        nameToUuid.put(name.toLowerCase(java.util.Locale.ROOT), uuid);
        uuidToName.put(uuid, name);
        trim(nameToUuid);
        trim(uuidToName);
    }

    private static void trim(Map<?, ?> map) {
        java.util.Iterator<?> it = map.keySet().iterator();
        while (map.size() > 200 && it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    public CompletableFuture<Optional<UUID>> resolveUuid(String name) {
        return CompletableFuture.supplyAsync(() -> {
            Player online = Bukkit.getPlayerExact(name);
            if (online != null) {
                remember(online.getUniqueId(), online.getName());
                return Optional.of(online.getUniqueId());
            }
            synchronized (this) {
                UUID cached = nameToUuid.get(name.toLowerCase(java.util.Locale.ROOT));
                if (cached != null) {
                    return Optional.of(cached);
                }
            }
            OfflinePlayer cachedOffline = getCachedOffline(name);
            if (cachedOffline != null && cachedOffline.getUniqueId() != null) {
                remember(cachedOffline.getUniqueId(), name);
                return Optional.of(cachedOffline.getUniqueId());
            }
            UUID lp = luckPermsUuid(name);
            if (lp != null) {
                remember(lp, name);
                return Optional.of(lp);
            }
            UUID fromFiles = scanFilesForUuid(name);
            if (fromFiles != null) {
                remember(fromFiles, name);
                return Optional.of(fromFiles);
            }
            if (!onlineMode) {
                UUID spoof = UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                remember(spoof, name);
                return Optional.of(spoof);
            }
            OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (op.getUniqueId() != null && (op.hasPlayedBefore() || op.isOnline())) {
                remember(op.getUniqueId(), name);
                return Optional.of(op.getUniqueId());
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<Optional<String>> resolveName(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                remember(uuid, online.getName());
                return Optional.of(online.getName());
            }
            synchronized (this) {
                String cached = uuidToName.get(uuid);
                if (cached != null) {
                    return Optional.of(cached);
                }
            }
            String lp = luckPermsName(uuid);
            if (lp != null) {
                remember(uuid, lp);
                return Optional.of(lp);
            }
            String fromFile = readLastKnownName(uuid);
            if (fromFile != null) {
                remember(uuid, fromFile);
                return Optional.of(fromFile);
            }
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            if (op.getName() != null) {
                remember(uuid, op.getName());
                return Optional.of(op.getName());
            }
            return Optional.empty();
        });
    }

    private OfflinePlayer getCachedOffline(String name) {
        try {
            return Bukkit.getOfflinePlayerIfCached(name);
        } catch (NoSuchMethodError e) {
            return null;
        }
    }

    private UUID luckPermsUuid(String name) {
        try {
            LuckPerms lp = LuckPermsProvider.get();
            return lp.getUserManager().lookupUniqueId(name).join();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String luckPermsName(UUID uuid) {
        try {
            LuckPerms lp = LuckPermsProvider.get();
            return lp.getUserManager().lookupUsername(uuid).join();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private File playerDir() {
        try {
            return access.playerDataDir();
        } catch (Throwable t) {
            return null;
        }
    }

    private String readLastKnownName(UUID uuid) {
        File dir = playerDir();
        if (dir == null || !dir.isDirectory()) {
            return null;
        }
        File file = new File(dir, uuid + ".dat");
        if (!file.isFile()) {
            return null;
        }
        return access.lastKnownNameOf(file);
    }

    private UUID scanFilesForUuid(String name) {
        File dir = playerDir();
        if (dir == null || !dir.isDirectory()) {
            return null;
        }
        File[] files = dir.listFiles((d, n) -> n.endsWith(".dat"));
        if (files == null) {
            return null;
        }
        for (File file : files) {
            String known = access.lastKnownNameOf(file);
            if (known != null && known.equalsIgnoreCase(name)) {
                String base = file.getName().substring(0, file.getName().length() - 4);
                try {
                    return UUID.fromString(base);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }
}

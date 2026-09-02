package teacommontea.veritesauver.invsee;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public final class InvSee {

    private final Plugin plugin;
    private final Config config;
    private final Resolver resolver;
    private final OfflineInventories offline;
    private final InvSeeListener listener;
    private final InvSeeClassgen gen;
    private final InvSeeAccess access;
    private final InvSeeMenuRoles menuRoles;

    public InvSee(Plugin plugin, Config config, Resolver resolver, InvSeeListener listener,
                  OfflineInventories offline, InvSeeClassgen gen, InvSeeMenuRoles menuRoles) {
        this.plugin = plugin;
        this.config = config;
        this.resolver = resolver;
        this.listener = listener;
        this.offline = offline;
        this.gen = gen;
        this.access = gen.access();
        this.menuRoles = menuRoles;
    }

    public OfflineInventories offline() {
        return offline;
    }

    public Plugin plugin() {
        return plugin;
    }

    public Resolver resolver() {
        return resolver;
    }

    public Config config() {
        return config;
    }

    public CompletableFuture<SpectateResult> openMain(org.bukkit.entity.Player spectator, String rawArg,
                                                      UUID uuid, String name, boolean bypassExempt) {
        return open(spectator, rawArg, uuid, name, bypassExempt, false);
    }

    public CompletableFuture<SpectateResult> openEnder(org.bukkit.entity.Player spectator, String rawArg,
                                                       UUID uuid, String name, boolean bypassExempt) {
        return open(spectator, rawArg, uuid, name, bypassExempt, true);
    }

    private CompletableFuture<SpectateResult> open(org.bukkit.entity.Player spectator, String rawArg,
                                                   UUID knownUuid, String knownName, boolean bypassExempt,
                                                   boolean ender) {
        CompletableFuture<Optional<UUID>> uuidFuture = knownUuid != null
                ? CompletableFuture.completedFuture(Optional.of(knownUuid))
                : resolver.resolveUuid(knownName);

        return uuidFuture.thenCompose(maybeUuid -> {
            if (maybeUuid.isEmpty()) {
                return CompletableFuture.completedFuture(
                        SpectateResult.fail(SpectateResult.Reason.TARGET_DOES_NOT_EXIST));
            }
            UUID uuid = maybeUuid.get();
            CompletableFuture<String> nameFuture = knownName != null
                    ? CompletableFuture.completedFuture(knownName)
                    : resolver.resolveName(uuid).thenApply(o -> o.orElse("InvSee Player"));
            return nameFuture.thenCompose(name ->
                    onSpectatorThread(spectator, () -> finishOpen(spectator, uuid, name, bypassExempt, ender)));
        });
    }

    private SpectateResult finishOpen(org.bukkit.entity.Player spectator, UUID uuid, String name,
                                      boolean bypassExempt, boolean ender) {
        String node = ender ? Exemption.ENDERSEE_NODE : Exemption.INVSEE_NODE;
        if (!bypassExempt && Exemption.isExempt(uuid, node)) {
            return SpectateResult.fail(SpectateResult.Reason.TARGET_EXEMPT);
        }

        try {
            org.bukkit.entity.Player onlineTarget = Bukkit.getPlayer(uuid);
            if (onlineTarget != null) {
                Object nmsTarget = access.handleOf(onlineTarget);
                return openWindow(spectator, uuid, name, ender, nmsTarget, null);
            }

            if (!config.offlineSupport) {
                return SpectateResult.fail(SpectateResult.Reason.OFFLINE_SUPPORT_DISABLED);
            }

            OfflineInventories.Loaded loaded = offline.load(uuid, name, config.unknownSupport,
                    ender ? OfflineInventories.Kind.ENDER : OfflineInventories.Kind.MAIN);
            if (loaded instanceof OfflineInventories.Loaded.Unknown) {
                return SpectateResult.fail(SpectateResult.Reason.UNKNOWN_TARGET);
            }
            OfflineInventories.Loaded.Ok ok = (OfflineInventories.Loaded.Ok) loaded;
            return openWindow(spectator, uuid, name, ender, ok.human(), ok);
        } catch (Throwable t) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "InvSee open failed", t);
            return SpectateResult.fail(SpectateResult.Reason.UNKNOWN);
        }
    }

    private SpectateResult openWindow(org.bukkit.entity.Player spectator, UUID uuid, String name,
                                      boolean ender, Object nmsTarget, OfflineInventories.Loaded.Ok offlineData)
            throws Throwable {
        Object nmsSpectator = access.handleOf(spectator);
        String title = config.titleFor(ender, name);
        Palette palette = config.palette();

        SpectateLog log = new SpectateLog(plugin, name, spectator.getName());

        Object provider = ender
                ? enderProvider(nmsTarget, offlineData, spectator, nmsSpectator, title, palette, log)
                : mainProvider(nmsTarget, offlineData, spectator, nmsSpectator, title, palette, log);

        listener.markSpectating(spectator.getUniqueId(), ender);
        OptionalInt result = access.openMenu(nmsSpectator, provider);
        if (result.isEmpty()) {
            return SpectateResult.fail(SpectateResult.Reason.OPEN_CANCELLED);
        }
        if (offlineData != null) {
            OfflineInventories.Kind kind = ender
                    ? OfflineInventories.Kind.ENDER : OfflineInventories.Kind.MAIN;
            listener.trackOffline(spectator.getUniqueId(), uuid, name, kind, offlineData.view());
        }
        return SpectateResult.success(provider);
    }

    private Object mainProvider(Object nmsTarget, OfflineInventories.Loaded.Ok offlineData,
                                org.bukkit.entity.Player spectator, Object nmsSpectator, String title,
                                Palette palette, SpectateLog log) throws Throwable {
        MainViewContainer top = offlineData != null
                ? (MainViewContainer) offlineData.view()
                : new MainViewContainer(access, nmsTarget);
        listener.trackMain(spectator.getUniqueId(), top);
        Mirror mirror = config.mainMirror();
        boolean own = nmsSpectator.equals(nmsTarget);
        Object display = access.titleComponent(title);
        return menuProvider(display, (id, playerInv) -> {
            MenuHandlers.Main handler = new MenuHandlers.Main(gen, menuRoles, top, log,
                    nmsSpectator, playerInv, mirror, palette, own);
            Object genContainer = gen.newContainer(top);
            handler.genContainerRef = genContainer;
            Object menuType = access.menuTypeForRows(5);
            Object menu = gen.newMenu(menuType, id, handler);
            handler.layout(menu, genContainer);
            return menu;
        });
    }

    private Object enderProvider(Object nmsTarget, OfflineInventories.Loaded.Ok offlineData,
                                 org.bukkit.entity.Player spectator, Object nmsSpectator, String title,
                                 Palette palette, SpectateLog log) throws Throwable {
        EnderViewContainer top = offlineData != null
                ? (EnderViewContainer) offlineData.view()
                : new EnderViewContainer(access, nmsTarget);
        int size = top.getContainerSize();
        int rows = Math.max(1, size / 9);
        Mirror mirror = config.enderMirror(size);
        Object display = access.titleComponent(title);
        return menuProvider(display, (id, playerInv) -> {
            MenuHandlers.Ender handler = new MenuHandlers.Ender(gen, menuRoles, top, log,
                    nmsSpectator, playerInv, mirror, palette, rows);
            Object genContainer = gen.newContainer(top);
            handler.genContainerRef = genContainer;
            Object menuType = access.menuTypeForRows(rows);
            Object menu = gen.newMenu(menuType, id, handler);
            handler.layout(menu, genContainer);
            return menu;
        });
    }

    private interface MenuFactory {
        Object create(int id, Object playerInv) throws Throwable;
    }

    private Object menuProvider(Object display, MenuFactory factory) {
        Class<?> providerClass = access.menuProviderClass();
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ providerClass },
                (proxy, method, methodArgs) -> {
                    switch (method.getName()) {
                        case "getDisplayName":
                            return display;
                        case "createMenu": {
                            int id = (int) methodArgs[0];
                            Object playerInv = methodArgs[1];
                            try {
                                return factory.create(id, playerInv);
                            } catch (Throwable t) {
                                plugin.getLogger().log(java.util.logging.Level.SEVERE,
                                        "InvSee menu build failed", t);
                                return null;
                            }
                        }
                        case "shouldTriggerClientSideContainerClose":
                            return false;
                        default:
                            return defaultReturn(method.getReturnType());
                    }
                });
    }

    private static Object defaultReturn(Class<?> ret) {
        if (ret == boolean.class) {
            return false;
        }
        if (ret == int.class) {
            return 0;
        }
        return null;
    }

    private <T> CompletableFuture<T> onSpectatorThread(org.bukkit.entity.Player spectator,
                                                       java.util.function.Supplier<T> body) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (!teacommontea.util.sched.Sched.regionised() && Bukkit.isPrimaryThread()) {
            future.complete(body.get());
        } else {
            teacommontea.util.sched.Sched.executeFor(spectator, () -> {
                try {
                    future.complete(body.get());
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        }
        return future;
    }
}

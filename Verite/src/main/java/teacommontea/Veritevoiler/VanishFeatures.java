package teacommontea.veritevoiler;

import teacommontea.util.Colours;
import org.bukkit.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import teacommontea.util.text.Text;
import teacommontea.util.chat.AdvancementMessage;

import java.util.UUID;

public final class VanishFeatures {

    private VanishFeatures() {}


    static void registerAll(Plugin plugin, Vanish vanish) {
        Bukkit.getPluginManager().registerEvents(new Prevent(vanish), plugin);
        Bukkit.getPluginManager().registerEvents(new FakeMessage(vanish), plugin);

        Bukkit.getPluginManager().registerEvents(new OnVanishState(plugin, vanish), plugin);
        Bukkit.getPluginManager().registerEvents(new InventoryInspect(vanish), plugin);
        PingHider.install(plugin, vanish);
        Bukkit.getPluginManager().registerEvents(new GameModeToggle(plugin, vanish), plugin);
        Bukkit.getPluginManager().registerEvents(new RideEntity(vanish), plugin);
        Bukkit.getPluginManager().registerEvents(new SilentContainer(plugin, vanish), plugin);
        startActionbar(plugin, vanish);
        vanish.setMuffler(VanishPacketInterceptor.install(plugin, vanish, vanish.settings()));
        VanishPlaceholders.registerIfAvailable();
        VanishTab.installIfAvailable(plugin, vanish);
    }

    private static boolean v(Vanish vanish, UUID id) {
        return vanish.enabled() && vanish.isVanished(id);
    }

    public static final class Prevent implements Listener {
        private final Vanish vanish;
        Prevent(Vanish vanish) {
            this.vanish = vanish;
            teacommontea.util.chat.ChatRouter.register(EventPriority.LOW, true, event -> {
                if (!s().preventChat) return;
                Player sender = event.sender();
                if (!v(vanish, sender.getUniqueId())) return;
                event.recipients().removeIf(viewer ->
                        !viewer.equals(sender) && !Vanish.canSee(viewer, sender));
            });
        }

        private VanishSettings s() { return vanish.settings(); }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onPickup(EntityPickupItemEvent e) {
            if (s().preventPickup && e.getEntity() instanceof Player p && v(vanish, p.getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onInteract(PlayerInteractEvent e) {
            if (s().preventInteract && v(vanish, e.getPlayer().getUniqueId())
                    && e.getAction() == org.bukkit.event.block.Action.PHYSICAL) {
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBreak(BlockBreakEvent e) {
            if (s().preventBlockBreak && v(vanish, e.getPlayer().getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onPlace(BlockPlaceEvent e) {
            if (s().preventBlockPlace && v(vanish, e.getPlayer().getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onTarget(EntityTargetLivingEntityEvent e) {
            if (s().preventTarget && e.getTarget() instanceof Player p && v(vanish, p.getUniqueId())) {
                e.setCancelled(true);
                e.setTarget(null);
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onDamage(EntityDamageEvent e) {
            if (s().preventDamage && e.getEntity() instanceof Player p && v(vanish, p.getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onFood(FoodLevelChangeEvent e) {
            if (s().preventFood && e.getEntity() instanceof Player p && v(vanish, p.getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onAir(org.bukkit.event.entity.EntityAirChangeEvent e) {
            if (s().preventFood && e.getEntity() instanceof Player p && v(vanish, p.getUniqueId())
                    && e.getAmount() < p.getMaximumAir()) {
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onDrop(org.bukkit.event.player.PlayerDropItemEvent e) {
            if (s().preventDrop && v(vanish, e.getPlayer().getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent e) {
            if (s().preventBuckets && v(vanish, e.getPlayer().getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onBucketFill(org.bukkit.event.player.PlayerBucketFillEvent e) {
            if (s().preventBuckets && v(vanish, e.getPlayer().getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onDamageBy(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
            if (s().preventDamage && e.getDamager() instanceof Player p && v(vanish, p.getUniqueId())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onProjectile(org.bukkit.event.entity.ProjectileLaunchEvent e) {
            if (s().preventProjectiles && e.getEntity().getShooter() instanceof Player p
                    && v(vanish, p.getUniqueId())) {
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onAdvancement(org.bukkit.event.player.PlayerAdvancementDoneEvent e) {
            if (s().preventAdvancement && v(vanish, e.getPlayer().getUniqueId())) {
                AdvancementMessage.suppress(e);
            }
        }

    }

    public static final class FakeMessage implements Listener {
        private final Vanish vanish;
        FakeMessage(Vanish vanish) { this.vanish = vanish; }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onJoin(PlayerJoinEvent e) {
            if (vanish.settings().fakeMessage && v(vanish, e.getPlayer().getUniqueId())) {
                e.setJoinMessage(null);
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onQuit(PlayerQuitEvent e) {
            if (vanish.settings().fakeMessage && v(vanish, e.getPlayer().getUniqueId())) {
                e.setQuitMessage(null);
            }
        }
    }

    public static final class OnVanishState implements VanishEvents.Listener, Listener {
        private final Vanish vanish;
        OnVanishState(Plugin plugin, Vanish vanish) {
            this.vanish = vanish;
            VanishEvents.register(this);
        }

        private VanishSettings s() { return vanish.settings(); }

        @Override
        public void onVanishChange(UUID id, boolean nowVanished) {
            if (nowVanished) {
                applyVanished(id);
            } else {
                applyUnvanished(id);
            }
        }

        private void applyVanished(UUID id) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) return;
            VanishSettings s = s();
            if (s.selfView) {
                p.setInvisible(true);
            }
            p.setCollidable(false);
            p.setSleepingIgnored(true);
            if (s.fly && p.hasPermission("verite.vanish.fly")) {
                p.setAllowFlight(true);
                p.setFlying(true);
            }
            if (s.invulnerability && p.hasPermission("verite.vanish.invulnerable")) {
                p.setInvulnerable(true);
            }
            applyEffects(p);
        }

        private void applyUnvanished(UUID id) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) return;
            p.setInvisible(false);
            p.setCollidable(true);
            p.setSleepingIgnored(false);
            p.setInvulnerable(false);
            if (!p.hasPermission("verite.vanish.fly.keep")
                    && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                p.setFlying(false);
                p.setAllowFlight(false);
            }
            restoreEffects(p);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent e) {
            Player p = e.getPlayer();
            if (vanish.isVanished(p.getUniqueId())) {
                applyVanished(p.getUniqueId());
            } else if (p.isInvisible()) {
                p.setInvisible(false);
            }
        }

        private void applyEffects(Player p) {
            if (!s().effects || !p.hasPermission("verite.vanish.effects")) return;

            if (!vanish.effectStore().has(p.getUniqueId())) {
                vanish.effectStore().snapshot(p, EFFECT_TYPES);
            }
            for (org.bukkit.potion.PotionEffectType type : EFFECT_TYPES) {
                if (type != null) {
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            type, INFINITE_DURATION, 0, true, false, false));
                }
            }
        }

        private void restoreEffects(Player p) {
            for (org.bukkit.potion.PotionEffectType type : EFFECT_TYPES) {
                if (type != null) p.removePotionEffect(type);
            }
            vanish.effectStore().restore(p, EFFECT_TYPES);
        }
    }

    private static final org.bukkit.potion.PotionEffectType[] EFFECT_TYPES = {
            org.bukkit.potion.PotionEffectType.getByName("NIGHT_VISION"),
    };

    private static final int INFINITE_DURATION = resolveInfiniteDuration();

    private static int resolveInfiniteDuration() {
        try {
            return org.bukkit.potion.PotionEffect.class.getField("INFINITE_DURATION").getInt(null);
        } catch (Throwable t) {
            return Integer.MAX_VALUE;
        }
    }

    public static final class GameModeToggle implements Listener {
        private final Plugin plugin;
        private final Vanish vanish;
        private final java.util.Map<UUID, GameMode> saved = new java.util.HashMap<>();
        private final java.util.Set<UUID> sneakWindow = java.util.concurrent.ConcurrentHashMap.newKeySet();
        GameModeToggle(Plugin plugin, Vanish vanish) { this.plugin = plugin; this.vanish = vanish; }

        @EventHandler(ignoreCancelled = true)
        public void onSneak(org.bukkit.event.player.PlayerToggleSneakEvent e) {
            Player p = e.getPlayer();
            if (!vanish.settings().gamemode || !e.isSneaking() || !v(vanish, p.getUniqueId())) return;
            UUID id = p.getUniqueId();
            if (sneakWindow.contains(id)) {

                if (p.getGameMode() == GameMode.SPECTATOR) {
                    p.setGameMode(saved.getOrDefault(id, GameMode.SURVIVAL));
                } else {
                    saved.put(id, p.getGameMode());
                    p.setGameMode(GameMode.SPECTATOR);
                }
                sneakWindow.remove(id);
            } else {
                if (p.getGameMode() != GameMode.SPECTATOR) saved.put(id, p.getGameMode());
                sneakWindow.add(id);
                teacommontea.util.sched.Sched.executeGlobal(() -> sneakWindow.remove(id), 8L);
            }
        }
    }

    public static final class RideEntity implements Listener {
        private final Vanish vanish;
        RideEntity(Vanish vanish) { this.vanish = vanish; }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onMount(org.bukkit.event.player.PlayerInteractEntityEvent e) {
            if (!vanish.settings().rideEntity) return;
            for (org.bukkit.entity.Entity passenger : e.getRightClicked().getPassengers()) {
                if (passenger instanceof Player pp && v(vanish, pp.getUniqueId())) {
                    pp.leaveVehicle();
                }
            }
        }
    }

    public static final class SilentContainer implements Listener {
        private final Plugin plugin;
        private final Vanish vanish;
        private final java.util.Map<UUID, GameMode> restore = new java.util.concurrent.ConcurrentHashMap<>();
        SilentContainer(Plugin plugin, Vanish vanish) { this.plugin = plugin; this.vanish = vanish; }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onInteract(PlayerInteractEvent e) {
            Player p = e.getPlayer();
            if (!vanish.settings().silentContainer || !v(vanish, p.getUniqueId())) return;
            if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
            org.bukkit.block.Block b = e.getClickedBlock();
            if (b == null) return;
            if (p.getGameMode() == GameMode.SPECTATOR) return;
            if (b.getType() == org.bukkit.Material.ENDER_CHEST) {
                e.setCancelled(true);
                p.openInventory(p.getEnderChest());
                return;
            }
            if (!(b.getState() instanceof org.bukkit.block.Container container)) return;
            e.setCancelled(true);
            restore.put(p.getUniqueId(), p.getGameMode());
            p.setGameMode(GameMode.SPECTATOR);
            p.openInventory(container.getInventory());
            teacommontea.util.sched.Sched.executeFor(p, () -> {
                GameMode gm = restore.remove(p.getUniqueId());
                if (gm != null) p.setGameMode(gm);
            }, 1L);
        }
    }

    public static final class InventoryInspect implements Listener {
        private final Vanish vanish;
        InventoryInspect(Vanish vanish) { this.vanish = vanish; }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onInteractPlayer(org.bukkit.event.player.PlayerInteractEntityEvent e) {
            Player staff = e.getPlayer();
            if (!vanish.settings().inventoryInspect || !v(vanish, staff.getUniqueId())) return;
            if (!staff.isSneaking()) return;
            if (!(e.getRightClicked() instanceof Player target)) return;
            e.setCancelled(true);
            staff.openInventory(target.getInventory());
        }
    }


    private static void startActionbar(Plugin plugin, Vanish vanish) {
        teacommontea.util.sched.Sched.executeGlobalRepeating(() -> {
            if (!vanish.enabled() || !vanish.settings().actionbar) return;
            if (vanish.getVanished().isEmpty()) return;
            for (UUID id : vanish.getVanished()) {
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    teacommontea.util.sched.Sched.executeFor(p, () ->
                        Text.actionBar(p, Colours.BRAND + "You are " + Colours.BRAND_ACCENT_SECONDARY + "vanished"));
                }
            }
        }, 20L, 40L);
    }
}

package teacommontea.veritechasse.Reality;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak.BlockReach;
import teacommontea.veritechasse.Vanilla.PlayerInteraction.Tags.FoodValues;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class InteractionMonitor implements Listener {

    private final Logger logger;
    private final InteractionCheck check;

    private final Map<UUID, BreakSession> breaking = new HashMap<>();
    private final Map<UUID, Long> consumeStart = new HashMap<>();
    private final Map<UUID, Integer> foodLevel = new HashMap<>();
    private final Map<UUID, Float> saturation = new HashMap<>();
    private final Map<UUID, Integer> placeSequence = new HashMap<>();
    private final Map<UUID, Long> lastAttackTick = new HashMap<>();
    private final Map<UUID, Boolean> sprintingBefore = new HashMap<>();
    private final Map<UUID, Float> health = new HashMap<>();
    private final Map<UUID, Long> blockingSince = new HashMap<>();
    private final Map<String, Integer> suspectCounts = new HashMap<>();

    private boolean verbose = true;
    private long tick;

    public InteractionMonitor(JavaPlugin plugin, Protocol protocol) {
        this.logger = plugin.getLogger();
        this.check = new InteractionCheck(protocol);
    }

    public void setTick(long tick) {
        this.tick = tick;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        PlayerSnapshot snapshot = snapshot(player);
        Block block = event.getBlock();
        String blockName = nameOf(block);

        ItemStack tool = event.getItemInHand();
        float hardness = block.getType().getHardness();
        boolean preferred = block.isPreferredTool(tool);

        float perTick = this.check.perTickProgress(
            baseToolSpeed(tool, block),
            hardness,
            preferred,
            tool,
            player.getInventory().getHelmet(),
            snapshot.effects(),
            snapshot.inWater(),
            snapshot.supported());

        this.breaking.put(player.getUniqueId(), new BreakSession(
            block.getX(), block.getY(), block.getZ(), blockName,
            this.tick, perTick, event.getInstaBreak()));

        report(player, this.check.evaluateBreakStart(
            snapshot, block.getX(), block.getY(), block.getZ(), blockName,
            blockInteractionRange(player), isCreative(player)));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerSnapshot snapshot = snapshot(player);
        Block block = event.getBlock();
        BreakSession session = this.breaking.remove(player.getUniqueId());

        report(player, this.check.evaluateBreakFinish(
            snapshot, session,
            block.getX(), block.getY(), block.getZ(), nameOf(block),
            blockInteractionRange(player), isCreative(player)));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerSnapshot snapshot = snapshot(player);
        Block placed = event.getBlockPlaced();
        Block against = event.getBlockAgainst();
        String replaced = event.getBlockReplacedState() == null
            ? null
            : event.getBlockReplacedState().getType().name().toLowerCase(Locale.ROOT);

        int minimumY = placed.getWorld().getMinHeight();
        int maximumY = placed.getWorld().getMaxHeight() - 1;

        report(player, this.check.evaluatePlace(
            snapshot,
            placed.getX(), placed.getY(), placed.getZ(),
            against.getX(), against.getY(), against.getZ(),
            replaced,
            event.canBuild(),
            blockInteractionRange(player),
            isCreative(player),
            minimumY,
            maximumY));

        report(player, this.check.evaluateBlockKnowledge(
            placed.getWorld().isChunkLoaded(placed.getX() >> 4, placed.getZ() >> 4),
            true,
            true,
            placed.getY(),
            minimumY,
            maximumY));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getClickedBlock() != null
            && event.getInteractionPoint() != null) {
            Block clicked = event.getClickedBlock();
            UUID id = player.getUniqueId();
            Integer previous = this.placeSequence.get(id);
            int sequence = previous == null ? 0 : previous.intValue() + 1;
            this.placeSequence.put(id, Integer.valueOf(sequence));
            report(player, this.check.evaluatePlaceHit(
                event.getInteractionPoint().getX(),
                event.getInteractionPoint().getY(),
                event.getInteractionPoint().getZ(),
                clicked.getX(), clicked.getY(), clicked.getZ(),
                faceNameOf(event.getBlockFace()),
                previous == null ? -1 : previous.intValue(),
                sequence));
        }

        String itemName = item.getType().name().toLowerCase(Locale.ROOT);
        if (!FoodValues.isEdible(itemName)) {
            return;
        }
        this.consumeStart.put(player.getUniqueId(), Long.valueOf(this.tick));
        this.foodLevel.put(player.getUniqueId(), Integer.valueOf(player.getFoodLevel()));
        report(player, this.check.evaluateConsumeStart(
            itemName, player.getFoodLevel(), player.isInvulnerable()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        PlayerSnapshot snapshot = snapshot(player);

        String itemName = event.getItem() == null
            ? null
            : event.getItem().getType().name().toLowerCase(Locale.ROOT);

        Long start = this.consumeStart.remove(id);
        Integer before = this.foodLevel.get(id);
        int foodBefore = before == null ? player.getFoodLevel() : before.intValue();
        Float satBefore = this.saturation.get(id);

        report(player, this.check.evaluateConsume(
            snapshot,
            itemName,
            start == null ? -1L : start.longValue(),
            foodBefore,
            player.getFoodLevel(),
            satBefore == null ? player.getSaturation() : satBefore.floatValue(),
            player.getSaturation()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        UUID id = player.getUniqueId();
        Integer before = this.foodLevel.get(id);
        int foodBefore = before == null ? player.getFoodLevel() : before.intValue();
        int foodAfter = event.getFoodLevel();
        this.foodLevel.put(id, Integer.valueOf(foodAfter));

        boolean consumed = event.getItem() != null;
        boolean saturationEffect =
            player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SATURATION);
        report(player, this.check.evaluateFoodChange(
            foodBefore,
            foodAfter,
            player.getSaturation(),
            player.getExhaustion(),
            (float) player.getHealth(),
            consumed,
            saturationEffect,
            difficultyOf(player),
            naturalRegenerationOf(player),
            1));

        Float satBefore = this.saturation.put(
            id, Float.valueOf(player.getSaturation()));
        if (satBefore != null) {
            report(player, this.check.evaluateSaturationChange(
                satBefore.floatValue(),
                player.getSaturation(),
                consumed,
                saturationEffect,
                difficultyOf(player),
                naturalRegenerationOf(player),
                1));
        }

        report(player, this.check.evaluateSprintGate(
            player.isSprinting(), foodAfter, player.getAllowFlight()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED
            && event.getRegainReason() != EntityRegainHealthEvent.RegainReason.REGEN) {
            return;
        }
        float before = (float) player.getHealth();
        report(player, this.check.evaluateRegen(
            before,
            before + (float) event.getAmount(),
            naturalRegenerationOf(player),
            player.getFoodLevel(),
            player.getSaturation(),
            difficultyOf(player)));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        PlayerSnapshot snapshot = snapshot(victim);
        String damageType = event.getCause() == null
            ? "generic"
            : event.getCause().name().toLowerCase(Locale.ROOT);

        float armour = (float) attributeValue(victim, org.bukkit.attribute.Attribute.GENERIC_ARMOR);
        float toughness = (float) attributeValue(
            victim, org.bukkit.attribute.Attribute.GENERIC_ARMOR_TOUGHNESS);

        report(victim, this.check.evaluateDamage(
            (float) event.getDamage(),
            (float) event.getFinalDamage(),
            armour,
            toughness,
            victim.getInventory().getHelmet(),
            victim.getInventory().getChestplate(),
            victim.getInventory().getLeggings(),
            victim.getInventory().getBoots(),
            snapshot.effects(),
            damageType,
            damagesHelmet(damageType),
            (float) victim.getLastDamage(),
            victim.getNoDamageTicks(),
            false));

        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }
        EntityDamageByEntityEvent byEntity = (EntityDamageByEntityEvent) event;
        if (!(byEntity.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) byEntity.getDamager();
        PlayerSnapshot attackerSnapshot = snapshot(attacker);
        UUID attackerId = attacker.getUniqueId();

        Long lastAttack = this.lastAttackTick.put(attackerId, Long.valueOf(this.tick));
        int ticksSince = lastAttack == null
            ? Integer.MAX_VALUE
            : (int) Math.min((long) Integer.MAX_VALUE, this.tick - lastAttack.longValue());
        double attackSpeed = attributeValue(
            attacker, org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED);
        float strengthScale = attackStrengthScale(ticksSince, attackSpeed);

        double distanceSquared = attacker.getEyeLocation().toVector()
            .distanceSquared(victim.getLocation().toVector());

        Boolean sprintBefore = this.sprintingBefore.get(attackerId);
        report(attacker, this.check.evaluateAttack(
            attackerSnapshot,
            distanceSquared,
            isCreative(attacker),
            sprintBefore == null ? attacker.isSprinting() : sprintBefore.booleanValue(),
            attacker.isSprinting(),
            strengthScale,
            ticksSince,
            attackSpeed));
        this.sprintingBefore.put(attackerId, Boolean.valueOf(attacker.isSprinting()));

        report(attacker, this.check.evaluateClaimedCrit(
            attackerSnapshot,
            byEntity.isCritical(),
            strengthScale,
            true));

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        float outgoing = this.check.attackerOutgoingDamage(
            attackerSnapshot,
            weapon,
            victim.getType(),
            (float) attributeValue(
                attacker, org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE),
            strengthScale,
            0.0F,
            true);

        report(victim, this.check.evaluateExchange(
            outgoing,
            (float) event.getFinalDamage(),
            victim.isBlocking(),
            0.0D,
            false,
            shieldTicksHeld(victim),
            armour,
            toughness,
            this.check.protectionOf(
                victim.getInventory().getHelmet(),
                victim.getInventory().getChestplate(),
                victim.getInventory().getLeggings(),
                victim.getInventory().getBoots(),
                damageType),
            snapshot.effects(),
            damageType,
            damagesHelmet(damageType),
            victim.getInventory().getHelmet() != null,
            (float) victim.getLastDamage(),
            victim.getNoDamageTicks(),
            false));

        Float attackerHealthBefore = this.health.get(attackerId);
        float attackerLost = attackerHealthBefore == null
            ? 0.0F
            : attackerHealthBefore.floatValue() - (float) attacker.getHealth();
        this.health.put(attackerId, Float.valueOf((float) attacker.getHealth()));
        report(attacker, this.check.evaluateThorns(
            attackerLost, victim.getInventory().getChestplate()));
    }

    private static float attackStrengthScale(int ticksSinceLastAttack, double attackSpeed) {
        if (attackSpeed <= 0.0D) {
            return 1.0F;
        }
        double cooldownTicks = 20.0D / attackSpeed;
        if (cooldownTicks <= 0.0D) {
            return 1.0F;
        }
        double scale = ((double) ticksSinceLastAttack + 0.5D) / cooldownTicks;
        if (scale < 0.0D) {
            return 0.0F;
        }
        return scale > 1.0D ? 1.0F : (float) scale;
    }

    private int shieldTicksHeld(Player victim) {
        Long since = this.blockingSince.get(victim.getUniqueId());
        if (!victim.isBlocking()) {
            this.blockingSince.remove(victim.getUniqueId());
            return 0;
        }
        if (since == null) {
            this.blockingSince.put(victim.getUniqueId(), Long.valueOf(this.tick));
            return 0;
        }
        long held = this.tick - since.longValue();
        return held < 0L ? 0 : (int) Math.min((long) Integer.MAX_VALUE, held);
    }

    private static double attributeValue(
            Player player, org.bukkit.attribute.Attribute attribute) {
        org.bukkit.attribute.AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? 0.0D : instance.getValue();
    }

    private static boolean damagesHelmet(String damageType) {
        return "falling_block".equals(damageType) || "fallingblock".equals(damageType);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.breaking.remove(id);
        this.consumeStart.remove(id);
        this.foodLevel.remove(id);
        this.saturation.remove(id);
        this.placeSequence.remove(id);
        this.lastAttackTick.remove(id);
        this.sprintingBefore.remove(id);
        this.health.remove(id);
        this.blockingSince.remove(id);
    }

    private PlayerSnapshot snapshot(Player player) {
        return PlayerSnapshot.of(player, this.tick, 0);
    }

    private static String nameOf(Block block) {
        return block.getType().name().toLowerCase(Locale.ROOT);
    }

    private static String faceNameOf(org.bukkit.block.BlockFace face) {
        if (face == null) {
            return null;
        }
        String name = face.name().toLowerCase(Locale.ROOT);
        return teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerPlace.BlockFace
            .isFace(name) ? name : null;
    }

    private static boolean isCreative(Player player) {
        String mode = player.getGameMode().name().toLowerCase(Locale.ROOT);
        return "creative".equals(mode);
    }

    private static double blockInteractionRange(Player player) {
        return BlockReach.DEFAULT_BLOCK_INTERACTION_RANGE;
    }

    private static float baseToolSpeed(ItemStack tool, Block block) {
        if (tool == null) {
            return 1.0F;
        }
        return block.isPreferredTool(tool) ? toolTierSpeed(tool) : 1.0F;
    }

    private static float toolTierSpeed(ItemStack tool) {
        String name = tool.getType().name().toLowerCase(Locale.ROOT);
        if (name.startsWith("netherite_")) {
            return 9.0F;
        }
        if (name.startsWith("diamond_")) {
            return 8.0F;
        }
        if (name.startsWith("iron_")) {
            return 6.0F;
        }
        if (name.startsWith("stone_")) {
            return 4.0F;
        }
        if (name.startsWith("golden_")) {
            return 12.0F;
        }
        if (name.startsWith("wooden_")) {
            return 2.0F;
        }
        return 1.0F;
    }

    private static String difficultyOf(Player player) {
        return player.getWorld().getDifficulty().name().toLowerCase(Locale.ROOT);
    }

    private static boolean naturalRegenerationOf(Player player) {
        Boolean value = player.getWorld().getGameRuleValue(
            org.bukkit.GameRule.NATURAL_REGENERATION);
        return value == null || value.booleanValue();
    }

    private void report(Player player, List<Observation> observations) {
        for (Observation observation : observations) {
            if (observation.isContext()) {
                continue;
            }
            if (observation.isNote()) {
                if (this.verbose) {
                    this.logger.info("[" + player.getName() + "] note "
                        + observation.label() + ": " + observation.detail());
                }
                continue;
            }
            count(observation.label());
            this.logger.warning("[" + player.getName() + "] SUSPECT "
                + observation.label() + ": " + observation.detail());
        }
    }

    private void count(String label) {
        Integer existing = this.suspectCounts.get(label);
        this.suspectCounts.put(label,
            Integer.valueOf(existing == null ? 1 : existing.intValue() + 1));
    }

    public void reportTotals() {
        if (this.suspectCounts.isEmpty()) {
            this.logger.info("Interaction monitor: no suspect observations recorded.");
            return;
        }
        this.logger.info("Interaction monitor totals (highest first):");
        this.suspectCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue().intValue() - a.getValue().intValue())
            .forEach(entry -> this.logger.info(
                "  " + entry.getKey() + ": " + entry.getValue()));
    }
}

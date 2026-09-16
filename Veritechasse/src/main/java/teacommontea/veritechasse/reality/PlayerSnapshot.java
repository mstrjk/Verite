package teacommontea.veritechasse.Reality;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.AlwaysWaterBlocks;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.FluidDepth;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;

public final class PlayerSnapshot {

    private final UUID id;
    private final String name;

    private final double x;
    private final double y;
    private final double z;

    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;

    private final float yaw;
    private final float pitch;

    private final boolean sprinting;
    private final boolean sneaking;
    private final boolean swimming;
    private final boolean gliding;
    private final boolean flying;
    private final boolean mayFly;

    private final boolean inWater;
    private final boolean inLava;
    private final boolean shallowLava;
    private final boolean rocketActive;
    private final double fluidHeight;
    private final double eyeHeight;
    private final boolean submerged;
    private final boolean supported;
    private final boolean invulnerable;
    private final int noDamageTicks;
    private final String gameMode;

    private final String blockHere;
    private final String blockBelow;
    private final String pose;

    private final String worldName;
    private final boolean ultraWarm;

    private final double fallDistance;
    private final double health;

    private final ActiveEffects effects;
    private final ItemStack boots;
    private final ItemStack leggings;
    private final ItemStack chestplate;
    private final ItemStack helmet;
    private final ItemStack heldItem;

    private final long tick;
    private final int packetIndex;

    private double observedHorizontal;
    private double observedVertical;

    private double authorisedHorizontal;
    private double authorisedVertical;

    private PlayerSnapshot(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.deltaX = builder.deltaX;
        this.deltaY = builder.deltaY;
        this.deltaZ = builder.deltaZ;
        this.yaw = builder.yaw;
        this.pitch = builder.pitch;
        this.sprinting = builder.sprinting;
        this.sneaking = builder.sneaking;
        this.swimming = builder.swimming;
        this.gliding = builder.gliding;
        this.flying = builder.flying;
        this.mayFly = builder.mayFly;
        this.inWater = builder.inWater;
        this.inLava = builder.inLava;
        this.shallowLava = builder.shallowLava;
        this.rocketActive = builder.rocketActive;
        this.fluidHeight = builder.fluidHeight;
        this.eyeHeight = builder.eyeHeight;
        this.submerged = builder.submerged;
        this.supported = builder.supported;
        this.invulnerable = builder.invulnerable;
        this.noDamageTicks = builder.noDamageTicks;
        this.gameMode = builder.gameMode;
        this.blockHere = builder.blockHere;
        this.blockBelow = builder.blockBelow;
        this.pose = builder.pose;
        this.worldName = builder.worldName;
        this.ultraWarm = builder.ultraWarm;
        this.fallDistance = builder.fallDistance;
        this.health = builder.health;
        this.effects = builder.effects;
        this.boots = builder.boots;
        this.leggings = builder.leggings;
        this.chestplate = builder.chestplate;
        this.helmet = builder.helmet;
        this.heldItem = builder.heldItem;
        this.tick = builder.tick;
        this.packetIndex = builder.packetIndex;
    }

    public static PlayerSnapshot of(Player player, long tick, int packetIndex) {
        Location location = player.getLocation();
        Builder builder = new Builder();
        builder.id = player.getUniqueId();
        builder.name = player.getName();
        builder.x = location.getX();
        builder.y = location.getY();
        builder.z = location.getZ();
        builder.deltaX = player.getVelocity().getX();
        builder.deltaY = player.getVelocity().getY();
        builder.deltaZ = player.getVelocity().getZ();
        builder.yaw = location.getYaw();
        builder.pitch = location.getPitch();
        builder.sprinting = player.isSprinting();
        builder.sneaking = player.isSneaking();
        builder.swimming = player.isSwimming();
        builder.gliding = player.isGliding();
        builder.flying = player.isFlying();
        builder.mayFly = player.getAllowFlight();
        builder.inWater = isInWater(player);
        builder.inLava = isInLava(player);
        builder.shallowLava = isShallowLava(player);
        builder.submerged = isSubmerged(player);
        builder.rocketActive = hasAttachedFirework(player);
        builder.fluidHeight = fluidHeightAt(player);
        builder.eyeHeight = player.getEyeHeight();
        builder.supported = SupportingBlock.present(player);
        builder.invulnerable = player.isInvulnerable();
        builder.noDamageTicks = player.getNoDamageTicks();
        builder.gameMode = player.getGameMode().name().toLowerCase(Locale.ROOT);
        builder.blockHere = blockNameAt(player, 0);
        builder.blockBelow = blockNameAt(player, -1);
        builder.pose = player.getPose().name().toLowerCase(Locale.ROOT);
        builder.worldName = location.getWorld() == null ? "" : location.getWorld().getName();
        builder.ultraWarm = location.getWorld() != null
            && location.getWorld().getEnvironment() == World.Environment.NETHER;
        builder.fallDistance = player.getFallDistance();
        builder.health = player.getHealth();
        builder.effects = effectsOf(player);
        builder.boots = player.getInventory().getBoots();
        builder.leggings = player.getInventory().getLeggings();
        builder.chestplate = player.getInventory().getChestplate();
        builder.helmet = player.getInventory().getHelmet();
        builder.heldItem = player.getInventory().getItemInMainHand();
        builder.tick = tick;
        builder.packetIndex = packetIndex;
        return new PlayerSnapshot(builder);
    }

    private static boolean isInLava(Player player) {
        String block = blockNameAt(player, 0);
        return "lava".equals(block);
    }

    public static final String WATER = "water";

    public static final String BUBBLE_COLUMN = "bubble_column";

    public static final int FLUID_LEVELS = 9;

    public static final int SOURCE_LEVEL = 0;

    public static final int FALLING_FLAG = 8;

    private static boolean isSubmerged(Player player) {
        Location eye = player.getEyeLocation();
        if (eye.getWorld() == null) {
            return false;
        }
        String atEye = eye.getBlock().getType().name().toLowerCase(Locale.ROOT);
        return AlwaysWaterBlocks.contains(atEye);
    }

    public static double fluidHeightAt(Player player) {
        World world = player.getWorld();
        org.bukkit.util.BoundingBox box = player.getBoundingBox();
        double entityY = box.getMinY();

        int x0 = floorOf(box.getMinX());
        int y0 = floorOf(box.getMinY());
        int z0 = floorOf(box.getMinZ());
        int x1 = ceilOf(box.getMaxX()) - 1;
        int y1 = ceilOf(box.getMaxY()) - 1;
        int z1 = ceilOf(box.getMaxZ()) - 1;

        double height = 0.0D;
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    double own = waterHeightOf(block);
                    if (own <= 0.0D) {
                        continue;
                    }
                    double fluidBottom = (double) y;
                    double fluidTop = fluidBottom + own;
                    if (fluidTop < box.getMinY()) {
                        continue;
                    }
                    double depth = fluidTop - entityY;
                    if (depth > height) {
                        height = depth;
                    }
                }
            }
        }
        return height;
    }

    private static int floorOf(double value) {
        int truncated = (int) value;
        return value < (double) truncated ? truncated - 1 : truncated;
    }

    private static int ceilOf(double value) {
        int truncated = (int) value;
        return value > (double) truncated ? truncated + 1 : truncated;
    }

    private static double waterHeightOf(Block block) {
        if (!isWaterBlock(block)) {
            return 0.0D;
        }
        if (isWaterBlock(block.getRelative(0, 1, 0))) {
            return 1.0D;
        }
        return ownHeightOf(block);
    }

    private static boolean isWaterBlock(Block block) {
        String name = block.getType().name().toLowerCase(Locale.ROOT);
        if (AlwaysWaterBlocks.contains(name)) {
            return true;
        }
        BlockData data = block.getBlockData();
        return data instanceof org.bukkit.block.data.Waterlogged
            && ((org.bukkit.block.data.Waterlogged) data).isWaterlogged();
    }

    private static double ownHeightOf(Block block) {
        String name = block.getType().name().toLowerCase(Locale.ROOT);
        if (BUBBLE_COLUMN.equals(name)) {
            return 1.0D;
        }
        BlockData data = block.getBlockData();
        if (!(data instanceof Levelled)) {
            return 1.0D;
        }
        int level = ((Levelled) data).getLevel();
        if (level >= FALLING_FLAG) {
            return 1.0D;
        }
        int amount = FLUID_LEVELS - 1 - level;
        return (double) amount / (double) FLUID_LEVELS;
    }

    private static boolean isInWater(Player player) {
        if (player.isInWater() || player.isSwimming()) {
            return true;
        }
        String here = blockNameAt(player, 0);
        if (AlwaysWaterBlocks.contains(here)) {
            return true;
        }
        String feet = blockNameAt(player, -1);
        return AlwaysWaterBlocks.contains(feet);
    }

    private static boolean isShallowLava(Player player) {
        if (!isInLava(player)) {
            return false;
        }
        return !"lava".equals(blockNameAt(player, 1));
    }

    public static final double FIREWORK_SEARCH_RADIUS = 1.0D;

    private static boolean hasAttachedFirework(Player player) {
        if (!player.isGliding()) {
            return false;
        }
        for (Entity nearby : player.getNearbyEntities(
                FIREWORK_SEARCH_RADIUS, FIREWORK_SEARCH_RADIUS, FIREWORK_SEARCH_RADIUS)) {
            if (nearby instanceof Firework) {
                return true;
            }
        }
        return false;
    }

    private static String blockNameAt(Player player, int offset) {
        Location location = player.getLocation();
        if (location.getWorld() == null) {
            return "";
        }
        Location target = location.clone().add(0.0D, offset, 0.0D);
        return target.getBlock().getType().name().toLowerCase(Locale.ROOT);
    }

    private static ActiveEffects effectsOf(Player player) {
        Map<String, Integer> amplifiers = new HashMap<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            amplifiers.put(
                registryNameOf(effect.getType()),
                Integer.valueOf(effect.getAmplifier()));
        }
        return ActiveEffects.of(amplifiers);
    }

    private static String registryNameOf(PotionEffectType type) {
        return type.getKey().getKey().toLowerCase(Locale.ROOT);
    }

    public UUID id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public double deltaX() {
        return this.deltaX;
    }

    public double deltaY() {
        return this.deltaY;
    }

    public double deltaZ() {
        return this.deltaZ;
    }

    public double horizontal() {
        return Math.sqrt(this.deltaX * this.deltaX + this.deltaZ * this.deltaZ);
    }

    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    public boolean sprinting() {
        return this.sprinting;
    }

    public boolean sneaking() {
        return this.sneaking;
    }

    public boolean swimming() {
        return this.swimming;
    }

    public boolean gliding() {
        return this.gliding;
    }

    public boolean flying() {
        return this.flying;
    }

    public boolean mayFly() {
        return this.mayFly;
    }

    public boolean inWater() {
        return this.inWater;
    }

    public boolean inLava() {
        return this.inLava;
    }

    public boolean supported() {
        return this.supported;
    }

    public boolean invulnerable() {
        return this.invulnerable;
    }

    public int noDamageTicks() {
        return this.noDamageTicks;
    }

    public String gameMode() {
        return this.gameMode;
    }

    public boolean takesFallDamage() {
        if (this.invulnerable || this.noDamageTicks > 0) {
            return false;
        }
        return !"creative".equals(this.gameMode) && !"spectator".equals(this.gameMode);
    }

    public String blockHere() {
        return this.blockHere;
    }

    public String blockBelow() {
        return this.blockBelow;
    }

    public String pose() {
        return this.pose;
    }

    public String worldName() {
        return this.worldName;
    }

    public boolean ultraWarm() {
        return this.ultraWarm;
    }

    public double fallDistance() {
        return this.fallDistance;
    }

    public double health() {
        return this.health;
    }

    public ActiveEffects effects() {
        return this.effects;
    }

    public ItemStack boots() {
        return this.boots;
    }

    public ItemStack leggings() {
        return this.leggings;
    }

    public ItemStack chestplate() {
        return this.chestplate;
    }

    public ItemStack helmet() {
        return this.helmet;
    }

    public ItemStack heldItem() {
        return this.heldItem;
    }

    public long tick() {
        return this.tick;
    }

    public int packetIndex() {
        return this.packetIndex;
    }

    public long sequence() {
        return this.tick * 100L + this.packetIndex;
    }

    public double observedHorizontal() {
        return this.observedHorizontal;
    }

    public void recordObservedHorizontal(double value) {
        this.observedHorizontal = value;
        this.authorisedHorizontal = value;
    }

    public double authorisedHorizontal() {
        return this.authorisedHorizontal;
    }

    public void limitAuthorisedHorizontal(double legalBound) {
        double capped = Math.min(this.observedHorizontal, legalBound);
        this.authorisedHorizontal = capped;
    }

    public double authorisedVertical() {
        return this.authorisedVertical;
    }

    public void limitAuthorisedVertical(double legalBound) {
        if (this.authorisedVertical > legalBound) {
            this.authorisedVertical = legalBound;
        }
    }

    public double observedVertical() {
        return this.observedVertical;
    }

    public void recordObservedVertical(double value) {
        this.observedVertical = value;
        this.authorisedVertical = value;
    }

    public double horizontalDistanceTo(PlayerSnapshot other) {
        double dx = this.x - other.x;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public double verticalDistanceTo(PlayerSnapshot other) {
        return this.y - other.y;
    }

    public boolean changedWorld(PlayerSnapshot other) {
        return !this.worldName.equals(other.worldName);
    }

    public static final double COLLISION_EPSILON = 1.0E-5D;

    public boolean horizontalCollisionSince(PlayerSnapshot previous) {
        double movedX = Math.abs(this.x - previous.x);
        double movedZ = Math.abs(this.z - previous.z);
        double intendedX = Math.abs(previous.deltaX);
        double intendedZ = Math.abs(previous.deltaZ);
        boolean arrestedX = intendedX > COLLISION_EPSILON
            && movedX < intendedX - COLLISION_EPSILON;
        boolean arrestedZ = intendedZ > COLLISION_EPSILON
            && movedZ < intendedZ - COLLISION_EPSILON;
        return arrestedX || arrestedZ;
    }

    public boolean roseSince(PlayerSnapshot previous) {
        return this.y > previous.y;
    }

    public boolean shallowLava() {
        return this.shallowLava;
    }

    public boolean submerged() {
        return this.submerged;
    }

    public boolean rocketActive() {
        return this.rocketActive;
    }

    public double fluidHeight() {
        return this.fluidHeight;
    }

    public double eyeHeight() {
        return this.eyeHeight;
    }

    public boolean swimPhysics() {
        if (this.submerged) {
            return true;
        }
        return FluidDepth.swimPhysicsApply(this.fluidHeight, this.eyeHeight);
    }

    public boolean touchingWater() {
        return this.inWater;
    }

    public boolean floatingInWater() {
        return this.inWater && !this.supported;
    }

    private static final class Builder {
        private UUID id;
        private String name;
        private double x;
        private double y;
        private double z;
        private double deltaX;
        private double deltaY;
        private double deltaZ;
        private float yaw;
        private float pitch;
        private boolean sprinting;
        private boolean sneaking;
        private boolean swimming;
        private boolean gliding;
        private boolean flying;
        private boolean mayFly;
        private boolean inWater;
        private boolean inLava;
        private boolean shallowLava;
        private boolean rocketActive;
        private double fluidHeight;
        private double eyeHeight;
        private boolean submerged;
        private boolean supported;
        private boolean invulnerable;
        private int noDamageTicks;
        private String gameMode;
        private String blockHere;
        private String blockBelow;
        private String pose;
        private String worldName;
        private boolean ultraWarm;
        private double fallDistance;
        private double health;
        private ActiveEffects effects;
        private ItemStack boots;
        private ItemStack leggings;
        private ItemStack chestplate;
        private ItemStack helmet;
        private ItemStack heldItem;
        private long tick;
        private int packetIndex;
    }
}

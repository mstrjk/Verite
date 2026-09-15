package teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Camel;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.HappyGhast;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Horses;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Llamas;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Nautilus;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Pig;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Strider;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BlockMovementFactors;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.EffectResolver;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class MountEffects {

    public static final boolean LIVING_MOUNTS_ACCEPT_EFFECTS = true;

    private MountEffects() {
    }

    public static boolean acceptsEffects(String entityName) {
        return MountControl.envelopeFor(entityName) != null;
    }

    public static double baseMovementSpeed(String entityName) {
        if (entityName == null) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (name.equals(Pig.KEY)) {
            return Pig.MOVEMENT_SPEED_BASE;
        }
        if (name.equals(Strider.KEY)) {
            return Strider.MOVEMENT_SPEED_BASE;
        }
        if (Camel.contains(name)) {
            return Camel.MOVEMENT_SPEED_BASE;
        }
        if (Nautilus.contains(name)) {
            return Nautilus.MOVEMENT_SPEED_BASE;
        }
        if (name.equals(HappyGhast.KEY)) {
            return HappyGhast.MOVEMENT_SPEED_BASE;
        }
        if (Llamas.contains(name)) {
            return Llamas.MOVEMENT_SPEED_BASE;
        }
        if (Horses.contains(name)) {
            return Horses.maximumMovementSpeed(name);
        }
        return 0.0D;
    }

    public static double baseJumpStrength(String entityName) {
        if (entityName == null) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (Camel.contains(name)) {
            return Camel.JUMP_STRENGTH_BASE;
        }
        if (Llamas.contains(name)) {
            return Llamas.JUMP_STRENGTH_BASE;
        }
        if (Horses.contains(name)) {
            return Horses.maximumJumpStrength(name);
        }
        return 0.0D;
    }

    public static double movementSpeed(String entityName, ActiveEffects effects) {
        double base = baseMovementSpeed(entityName);
        if (effects == null) {
            return base;
        }
        return EffectResolver.movementSpeed(base, effects);
    }

    public static double movementSpeed(String entityName, double baseSpeed, ActiveEffects effects) {
        if (effects == null) {
            return baseSpeed;
        }
        return EffectResolver.movementSpeed(baseSpeed, effects);
    }

    public static float jumpPower(String entityName, ActiveEffects effects, String blockBelow) {
        float base = (float) baseJumpStrength(entityName);
        float blockJumpFactor = BlockMovementFactors.jumpFactorOf(blockBelow);
        if (effects == null) {
            return base * blockJumpFactor;
        }
        return EffectResolver.jumpPower(base, blockJumpFactor, effects);
    }

    public static double riddenSpeed(
            String entityName,
            ActiveEffects effects,
            float boostFactor,
            boolean suffocating,
            boolean inWater,
            Protocol protocol) {
        if (entityName == null) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        double resolved = movementSpeed(name, effects);
        if (name.equals(Pig.KEY)) {
            return resolved * Pig.SPEED_MULTIPLIER * (double) boostFactor;
        }
        if (name.equals(Strider.KEY)) {
            return Strider.maxSpeed(resolved, suffocating, boostFactor, protocol);
        }
        if (Nautilus.contains(name)) {
            return Nautilus.terminalSpeed(resolved, inWater);
        }
        if (name.equals(HappyGhast.KEY)) {
            return HappyGhast.FLYING_SPEED_BASE * HappyGhast.RIDDEN_INPUT_SCALE;
        }
        return resolved;
    }

    public static Reality speedFrom(String entityName, ActiveEffects effects) {
        return Reality.of(movementSpeed(entityName, effects));
    }

    public static Reality riddenSpeedFrom(
            String entityName,
            ActiveEffects effects,
            float boostFactor,
            boolean suffocating,
            boolean inWater,
            Protocol protocol) {
        return Reality.of(
            riddenSpeed(entityName, effects, boostFactor, suffocating, inWater, protocol));
    }

    public static Reality jumpFrom(String entityName, ActiveEffects effects, String blockBelow) {
        if (!MountControl.canJump(entityName)) {
            return Reality.impossible();
        }
        return Reality.of(jumpPower(entityName, effects, blockBelow));
    }
}

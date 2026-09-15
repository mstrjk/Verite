package teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Camel;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.HappyGhast;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Horses;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Llamas;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Pig;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Strider;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BlockMovementFactors;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.EffectResolver;
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
        if (name.equals(Camel.KEY)) {
            return Camel.MOVEMENT_SPEED_BASE;
        }
        if (name.equals(HappyGhast.KEY)) {
            return HappyGhast.MOVEMENT_SPEED_BASE;
        }
        if (Llamas.contains(name)) {
            return Llamas.MOVEMENT_SPEED_BASE;
        }
        if (Horses.contains(name)) {
            return Horses.MAX_MOVEMENT_SPEED;
        }
        return 0.0D;
    }

    public static double baseJumpStrength(String entityName) {
        if (entityName == null) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (name.equals(Camel.KEY)) {
            return Camel.JUMP_STRENGTH_BASE;
        }
        if (Llamas.contains(name)) {
            return Llamas.JUMP_STRENGTH_BASE;
        }
        if (Horses.contains(name)) {
            return Horses.MAX_JUMP_STRENGTH;
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

    public static double riddenSpeed(String entityName, ActiveEffects effects, float boostFactor, boolean suffocating) {
        if (entityName == null) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        double resolved = movementSpeed(name, effects);
        if (name.equals(Pig.KEY)) {
            return resolved * Pig.SPEED_MULTIPLIER * boostFactor;
        }
        if (name.equals(Strider.KEY)) {
            return resolved * Strider.speedMultiplier(suffocating) * boostFactor;
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
            boolean suffocating) {
        return Reality.of(riddenSpeed(entityName, effects, boostFactor, suffocating));
    }

    public static Reality jumpFrom(String entityName, ActiveEffects effects, String blockBelow) {
        if (!MountControl.canJump(entityName)) {
            return Reality.impossible();
        }
        return Reality.of(jumpPower(entityName, effects, blockBelow));
    }
}

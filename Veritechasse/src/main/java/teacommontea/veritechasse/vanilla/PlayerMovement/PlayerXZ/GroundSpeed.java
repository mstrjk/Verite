package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.ControllableEntities.Support.GroundFriction;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.EffectResolver;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Support.PlayerBase;

public final class GroundSpeed {

    public static final float FRICTION_NUMERATOR = 0.21600002F;
    public static final float FRICTION_THRESHOLD = 0.6F;

    public static final float SPRINT_BONUS = 0.3F;

    public static final float AIR_DRAG = 0.91F;

    public static final float DEFAULT_FRICTION_MODIFIER = 1.0F;
    public static final float DEFAULT_AIR_DRAG_MODIFIER = 1.0F;
    public static final float MODIFIER_MINIMUM = 0.0F;
    public static final float MODIFIER_MAXIMUM = 2048.0F;

    public static final double BASE_MOVEMENT_SPEED = PlayerBase.MOVEMENT_SPEED;

    private GroundSpeed() {
    }

    public static float computeModifiedFriction(float friction, float modifier) {
        float result = 1.0F - (1.0F - friction) * modifier;
        if (result < 0.0F) {
            return 0.0F;
        }
        return result > 1.0F ? 1.0F : result;
    }

    public static float blockFriction(String blockBelow, boolean onGround, float frictionModifier) {
        if (!onGround) {
            return 1.0F;
        }
        return computeModifiedFriction(GroundFriction.of(blockBelow), frictionModifier);
    }

    public static float airDrag(float airDragModifier) {
        return computeModifiedFriction(AIR_DRAG, airDragModifier);
    }

    public static float horizontalDecay(String blockBelow, boolean onGround, float frictionModifier, float airDragModifier) {
        return blockFriction(blockBelow, onGround, frictionModifier) * airDrag(airDragModifier);
    }

    public static float horizontalDecay(String blockBelow) {
        return horizontalDecay(blockBelow, true, DEFAULT_FRICTION_MODIFIER, DEFAULT_AIR_DRAG_MODIFIER);
    }

    public static double attributeSpeed(ActiveEffects effects, boolean sprinting) {
        return attributeSpeed(effects, sprinting, null, false, 0.0F, Era.ENCHANTS_AS_DATA);
    }

    public static double attributeSpeed(
            ActiveEffects effects,
            boolean sprinting,
            ItemStack boots,
            boolean onSoulBlock,
            float percentFrozen,
            Era era) {
        double base = BASE_MOVEMENT_SPEED;
        base = base + MovementEfficiency.soulSpeedBonus(boots, onSoulBlock, era);
        base = base + MovementEfficiency.powderSnowSlowdown(percentFrozen);
        if (base < 0.0D) {
            base = 0.0D;
        }
        double withEffects = effects == null ? base : EffectResolver.movementSpeed(base, effects);
        if (!sprinting) {
            return withEffects;
        }
        return withEffects * (1.0D + SPRINT_BONUS);
    }

    public static final float AIRBORNE_SPEED = 0.02F;
    public static final float AIRBORNE_SPRINT_SPEED = 0.025999999F;
    public static final float CREATIVE_FLY_SPEED = 0.05F;
    public static final float CREATIVE_FLY_SPRINT_MULTIPLIER = 2.0F;

    public static float airborneSpeed(boolean sprinting, boolean creativeFlying) {
        if (creativeFlying) {
            return sprinting
                ? CREATIVE_FLY_SPEED * CREATIVE_FLY_SPRINT_MULTIPLIER
                : CREATIVE_FLY_SPEED;
        }
        return sprinting ? AIRBORNE_SPRINT_SPEED : AIRBORNE_SPEED;
    }

    public static float frictionInfluencedSpeed(double attributeSpeed, float blockFriction) {
        if (blockFriction > FRICTION_THRESHOLD) {
            double cubed = blockFriction * blockFriction * blockFriction;
            return (float) (attributeSpeed * (FRICTION_NUMERATOR / cubed));
        }
        return (float) attributeSpeed;
    }

    public static float frictionInfluencedSpeed(
            double attributeSpeed,
            float blockFriction,
            boolean onGround,
            boolean sprinting,
            boolean creativeFlying) {
        if (!onGround) {
            return airborneSpeed(sprinting, creativeFlying);
        }
        return frictionInfluencedSpeed(attributeSpeed, blockFriction);
    }

    public static float frictionInfluencedSpeed(ActiveEffects effects, boolean sprinting, String blockBelow) {
        return frictionInfluencedSpeed(attributeSpeed(effects, sprinting), GroundFriction.of(blockBelow));
    }

    public static double acceleration(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            double inputLength) {
        return frictionInfluencedSpeed(effects, sprinting, blockBelow) * inputLength;
    }

    public static double terminalSpeed(double acceleration, float decay) {
        if (decay >= 1.0F) {
            return Double.POSITIVE_INFINITY;
        }
        return acceleration / (1.0D - decay);
    }

    public static double terminalSpeed(ActiveEffects effects, boolean sprinting, String blockBelow, String blockHere) {
        return terminalSpeed(effects, sprinting, blockBelow, blockHere, null, 0.0F, Era.ENCHANTS_AS_DATA);
    }

    public static double terminalSpeed(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack boots,
            float percentFrozen,
            Era era) {
        return terminalSpeed(effects, sprinting, blockBelow, blockHere, boots, percentFrozen, true, false, era);
    }

    public static double terminalSpeed(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack boots,
            float percentFrozen,
            boolean onGround,
            boolean creativeFlying,
            Era era) {
        float decay = horizontalDecay(blockBelow, onGround, DEFAULT_FRICTION_MODIFIER, DEFAULT_AIR_DRAG_MODIFIER);
        boolean onSoulBlock = MovementEfficiency.isSoulBlock(blockBelow);
        double attribute = attributeSpeed(effects, sprinting, boots, onSoulBlock, percentFrozen, era);
        float friction = blockFriction(blockBelow, onGround, DEFAULT_FRICTION_MODIFIER);
        float speed = frictionInfluencedSpeed(attribute, friction, onGround, sprinting, creativeFlying);
        float speedFactor = onGround
            ? MovementEfficiency.effectiveSpeedFactor(blockHere, blockBelow, boots)
            : 1.0F;
        return terminalSpeed(speed * speedFactor, decay);
    }

    public static double speedAfterFriction(double horizontal, String blockBelow) {
        return horizontal * horizontalDecay(blockBelow);
    }

    public static Reality terminalFrom(ActiveEffects effects, boolean sprinting, String blockBelow, String blockHere) {
        return Reality.of(terminalSpeed(effects, sprinting, blockBelow, blockHere));
    }

    public static Reality accelerationFrom(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            double inputLength) {
        return Reality.of(acceleration(effects, sprinting, blockBelow, inputLength));
    }
}

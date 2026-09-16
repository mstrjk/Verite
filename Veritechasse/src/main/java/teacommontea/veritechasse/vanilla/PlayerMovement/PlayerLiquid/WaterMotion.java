package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.DepthStrider;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Potions.DolphinsGrace;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class WaterMotion {

    public static final float BASE_SLOW_DOWN = 0.8F;
    public static final float SPRINTING_SLOW_DOWN = 0.9F;
    public static final float DOLPHINS_GRACE_SLOW_DOWN = 0.96F;

    public static final float TARGET_SLOW_DOWN = 0.54600006F;

    public static final float BASE_SPEED = 0.02F;

    public static final float VERTICAL_DRAG = 0.8F;

    public static final float AIRBORNE_EFFICIENCY_SCALE = 0.5F;

    public static final float LEGACY_MAX_DEPTH_STRIDER = 3.0F;

    public static final double SWIM_JUMP = 0.04D;
    public static final double SINK_RATE = -0.04D;

    public static final double CLIMBABLE_COLLISION_BOOST = 0.2D;
    public static final double JUMP_OUT_OF_FLUID = 0.3D;
    public static final float JUMP_OUT_PROBE_HEIGHT_SOURCE = 0.6F;
    public static final double JUMP_OUT_PROBE_HEIGHT = JUMP_OUT_PROBE_HEIGHT_SOURCE;

    public static final int ATTRIBUTE_PROTOCOL_MAJOR = 1;
    public static final int ATTRIBUTE_PROTOCOL_MINOR = 21;
    public static final int ATTRIBUTE_PROTOCOL_PATCH = 0;

    private WaterMotion() {
    }

    public static boolean efficiencyIsAttributeDriven(Protocol protocol) {
        return protocol.atLeast(
            ATTRIBUTE_PROTOCOL_MAJOR,
            ATTRIBUTE_PROTOCOL_MINOR,
            ATTRIBUTE_PROTOCOL_PATCH);
    }

    public static float efficiencyFrom(ItemStack boots, Era era) {
        return DepthStrider.waterMovementEfficiency(DepthStrider.levelOn(boots), era);
    }

    public static float efficiencyFrom(int depthStriderLevel, Era era) {
        return DepthStrider.waterMovementEfficiency(depthStriderLevel, era);
    }

    public static float applyGroundScale(float efficiency, boolean onGround) {
        return onGround ? efficiency : efficiency * AIRBORNE_EFFICIENCY_SCALE;
    }

    public static float slowDown(
            boolean sprinting,
            float efficiency,
            boolean onGround,
            ActiveEffects effects) {
        float base = sprinting ? SPRINTING_SLOW_DOWN : BASE_SLOW_DOWN;
        float scaled = applyGroundScale(efficiency, onGround);
        if (scaled > 0.0F) {
            base = base + (TARGET_SLOW_DOWN - base) * scaled;
        }
        if (effects != null && effects.has(DolphinsGrace.KEY)) {
            return DOLPHINS_GRACE_SLOW_DOWN;
        }
        return base;
    }

    public static float acceleration(
            double attributeSpeed,
            float efficiency,
            boolean onGround) {
        float speed = BASE_SPEED;
        float scaled = applyGroundScale(efficiency, onGround);
        if (scaled > 0.0F) {
            speed = speed + ((float) attributeSpeed - speed) * scaled;
        }
        return speed;
    }

    public static double terminalSpeed(double acceleration, float slowDown) {
        if (slowDown >= 1.0F) {
            return Double.POSITIVE_INFINITY;
        }
        double drag = (double) slowDown;
        return acceleration * drag / (1.0D - drag);
    }

    public static double horizontalAfter(double component, float slowDown) {
        return component * (double) slowDown;
    }

    public static double verticalAfter(double component) {
        return component * (double) VERTICAL_DRAG;
    }

    public static boolean slowDownExceedsVanilla(float slowDown) {
        return slowDown > DOLPHINS_GRACE_SLOW_DOWN;
    }

    public static Reality slowDownFrom(
            boolean sprinting,
            float efficiency,
            boolean onGround,
            ActiveEffects effects) {
        return Reality.of(slowDown(sprinting, efficiency, onGround, effects));
    }
}

package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class JumpBoost {

    public static final String KEY = "jump_boost";
    public static final int VANILLA_MAX_AMPLIFIER = 1;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final float BOOST_PER_AMPLIFIER = 0.1F;
    public static final float LEGACY_BASE_JUMP_POWER = 0.42F;
    public static final float MINIMUM_JUMP_POWER = 1.0E-5F;
    public static final double SPRINT_IMPULSE = 0.2D;

    public static final double SAFE_FALL_DISTANCE_PER_AMPLIFIER = 1.0D;
    public static final Operation SAFE_FALL_OPERATION = Operation.ADD_VALUE;

    private JumpBoost() {
    }

    public static float boost(int amplifier) {
        return BOOST_PER_AMPLIFIER * ((float) amplifier + 1.0F);
    }

    public static boolean jumpStrengthIsAttributeDriven(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static boolean grantsSafeFallDistance(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static float jumpPower(float baseJumpStrength, float blockJumpFactor, int amplifier) {
        return baseJumpStrength * blockJumpFactor + boost(amplifier);
    }

    public static float jumpPower(float blockJumpFactor, int amplifier) {
        return jumpPower(LEGACY_BASE_JUMP_POWER, blockJumpFactor, amplifier);
    }

    public static boolean jumpSuppressed(float jumpPower, Protocol protocol) {
        if (!jumpStrengthIsAttributeDriven(protocol)) {
            return false;
        }
        return jumpPower <= MINIMUM_JUMP_POWER;
    }

    public static double appliedVelocity(double currentY, float jumpPower, Protocol protocol) {
        if (jumpStrengthIsAttributeDriven(protocol)) {
            return Math.max((double) jumpPower, currentY);
        }
        return jumpPower;
    }

    public static Modifier safeFallModifier(int amplifier) {
        return new Modifier(SAFE_FALL_DISTANCE_PER_AMPLIFIER, SAFE_FALL_OPERATION).at(amplifier);
    }

    public static double safeFallDistance(int amplifier, Protocol protocol) {
        if (!grantsSafeFallDistance(protocol)) {
            return Attributes.SAFE_FALL_DISTANCE.base();
        }
        return AttributePipeline.resolve(Attributes.SAFE_FALL_DISTANCE, safeFallModifier(amplifier));
    }

    public static Reality jumpPowerFrom(float baseJumpStrength, float blockJumpFactor, int amplifier) {
        return Reality.of(jumpPower(baseJumpStrength, blockJumpFactor, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}

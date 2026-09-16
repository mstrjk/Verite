package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerJump;

import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BlockMovementFactors;
import teacommontea.veritechasse.vanilla.Potions.JumpBoost;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class JumpPower {

    public static final float BASE_JUMP_STRENGTH = 0.42F;

    public static final float ATTRIBUTE_MINIMUM = 0.0F;
    public static final float ATTRIBUTE_MAXIMUM = 32.0F;

    public static final float SUPPRESSION_THRESHOLD = 1.0E-5F;

    public static final float BOOST_PER_AMPLIFIER = 0.1F;

    public static final int ATTRIBUTE_PROTOCOL_MAJOR = 1;
    public static final int ATTRIBUTE_PROTOCOL_MINOR = 20;
    public static final int ATTRIBUTE_PROTOCOL_PATCH = 5;

    public static final int MAXIMUM_PROTOCOL_MAJOR = 1;
    public static final int MAXIMUM_PROTOCOL_MINOR = 21;
    public static final int MAXIMUM_PROTOCOL_PATCH = 2;

    public static final int BOOST_INSIDE_POWER_MAJOR = 1;
    public static final int BOOST_INSIDE_POWER_MINOR = 20;
    public static final int BOOST_INSIDE_POWER_PATCH = 0;

    private JumpPower() {
    }

    public static boolean attributeDriven(Protocol protocol) {
        return protocol.atLeast(
            ATTRIBUTE_PROTOCOL_MAJOR,
            ATTRIBUTE_PROTOCOL_MINOR,
            ATTRIBUTE_PROTOCOL_PATCH);
    }

    public static boolean suppressionApplies(Protocol protocol) {
        return attributeDriven(protocol);
    }

    public static boolean usesMaximumNotSet(Protocol protocol) {
        return protocol.atLeast(
            MAXIMUM_PROTOCOL_MAJOR,
            MAXIMUM_PROTOCOL_MINOR,
            MAXIMUM_PROTOCOL_PATCH);
    }

    public static boolean boostAppliedInsideJumpPower(Protocol protocol) {
        return protocol.atLeast(
            BOOST_INSIDE_POWER_MAJOR,
            BOOST_INSIDE_POWER_MINOR,
            BOOST_INSIDE_POWER_PATCH);
    }

    public static float clampAttribute(double value) {
        return (float) Attributes.JUMP_STRENGTH_GENERIC.sanitize(value);
    }

    public static float boostPower(ActiveEffects effects) {
        if (effects == null) {
            return 0.0F;
        }
        int amplifier = effects.amplifierOf(JumpBoost.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return 0.0F;
        }
        return BOOST_PER_AMPLIFIER * ((float) amplifier + 1.0F);
    }

    public static float blockJumpFactor(String blockHere, String blockBelow) {
        return BlockMovementFactors.resolveJumpFactor(blockHere, blockBelow);
    }

    public static float of(
            double jumpStrengthAttribute,
            String blockHere,
            String blockBelow,
            ActiveEffects effects,
            Protocol protocol) {
        float factor = blockJumpFactor(blockHere, blockBelow);
        float boost = boostPower(effects);
        if (!attributeDriven(protocol)) {
            return BASE_JUMP_STRENGTH * factor + boost;
        }
        return clampAttribute(jumpStrengthAttribute) * factor + boost;
    }

    public static float of(String blockHere, String blockBelow, ActiveEffects effects, Protocol protocol) {
        return of(BASE_JUMP_STRENGTH, blockHere, blockBelow, effects, protocol);
    }

    public static boolean suppressed(float jumpPower, Protocol protocol) {
        if (!suppressionApplies(protocol)) {
            return false;
        }
        return jumpPower <= SUPPRESSION_THRESHOLD;
    }

    public static double appliedVertical(double currentDeltaY, float jumpPower, Protocol protocol) {
        if (suppressed(jumpPower, protocol)) {
            return currentDeltaY;
        }
        if (usesMaximumNotSet(protocol)) {
            return Math.max((double) jumpPower, currentDeltaY);
        }
        return jumpPower;
    }

    public static boolean exceedsVanillaDefault(float jumpPower) {
        return jumpPower > BASE_JUMP_STRENGTH;
    }

    public static Reality from(
            double jumpStrengthAttribute,
            String blockHere,
            String blockBelow,
            ActiveEffects effects,
            Protocol protocol) {
        return Reality.of(of(jumpStrengthAttribute, blockHere, blockBelow, effects, protocol));
    }
}

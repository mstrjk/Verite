package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerSneak;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.SwiftSneak;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Support.PlayerBase;

public final class SneakSpeed {

    public static final double BASE_SNEAKING_SPEED = PlayerBase.SNEAKING_SPEED;

    public static final double ATTRIBUTE_MINIMUM = 0.0D;
    public static final double ATTRIBUTE_MAXIMUM = 1.0D;

    public static final int ATTRIBUTE_PROTOCOL_MAJOR = 1;
    public static final int ATTRIBUTE_PROTOCOL_MINOR = 21;
    public static final int ATTRIBUTE_PROTOCOL_PATCH = 0;

    private SneakSpeed() {
    }

    public static boolean attributeExists(Protocol protocol) {
        return protocol.atLeast(
            ATTRIBUTE_PROTOCOL_MAJOR,
            ATTRIBUTE_PROTOCOL_MINOR,
            ATTRIBUTE_PROTOCOL_PATCH);
    }

    public static double clamp(double value) {
        if (Double.isNaN(value)) {
            return ATTRIBUTE_MINIMUM;
        }
        if (value < ATTRIBUTE_MINIMUM) {
            return ATTRIBUTE_MINIMUM;
        }
        return value > ATTRIBUTE_MAXIMUM ? ATTRIBUTE_MAXIMUM : value;
    }

    public static double factorFor(ItemStack leggings, Era era) {
        int level = SwiftSneak.levelOn(leggings);
        return clamp(SwiftSneak.sneakingSpeed(level, era));
    }

    public static double factorFor(int swiftSneakLevel, Era era) {
        return clamp(SwiftSneak.sneakingSpeed(swiftSneakLevel, era));
    }

    public static double factorFromAttribute(double attributeValue) {
        return clamp(attributeValue);
    }

    public static double baseFactor() {
        return BASE_SNEAKING_SPEED;
    }

    public static double apply(double inputComponent, double factor) {
        return inputComponent * clamp(factor);
    }

    public static double applyIfSlow(
            double inputComponent,
            boolean crouchingPose,
            boolean visuallyCrawling,
            double factor) {
        if (!SneakState.isMovingSlowly(crouchingPose, visuallyCrawling)) {
            return inputComponent;
        }
        return apply(inputComponent, factor);
    }

    public static boolean exceedsVanillaMaximum(double factor) {
        return factor > ATTRIBUTE_MAXIMUM;
    }

    public static Reality factorFrom(ItemStack leggings, Era era) {
        return Reality.of(factorFor(leggings, era));
    }
}

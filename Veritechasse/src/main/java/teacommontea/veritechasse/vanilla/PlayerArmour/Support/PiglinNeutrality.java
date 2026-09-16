package teacommontea.veritechasse.Vanilla.PlayerArmour.Support;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.PlayerArmour.Tags.PiglinSafeArmor;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class PiglinNeutrality {

    public static final int TAG_PROTOCOL_MAJOR = 1;
    public static final int TAG_PROTOCOL_MINOR = 21;
    public static final int TAG_PROTOCOL_PATCH = 3;

    private PiglinNeutrality() {
    }

    public static boolean safeWearable(ItemStack stack, Protocol protocol) {
        if (stack == null) {
            return false;
        }
        return PiglinSafeArmor.contains(stack.getType().name().toLowerCase(Locale.ROOT), protocol);
    }

    public static boolean isWearingSafeArmour(
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            Protocol protocol) {
        if (safeWearable(helmet, protocol)) {
            return true;
        }
        if (safeWearable(chestplate, protocol)) {
            return true;
        }
        if (safeWearable(leggings, protocol)) {
            return true;
        }
        return safeWearable(boots, protocol);
    }

    public static boolean angersPiglins(
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            Protocol protocol) {
        return !isWearingSafeArmour(helmet, chestplate, leggings, boots, protocol);
    }

    public static boolean usesTag(Protocol protocol) {
        return protocol.atLeast(TAG_PROTOCOL_MAJOR, TAG_PROTOCOL_MINOR, TAG_PROTOCOL_PATCH);
    }
}

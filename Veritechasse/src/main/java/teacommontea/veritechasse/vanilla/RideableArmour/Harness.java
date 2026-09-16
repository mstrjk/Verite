package teacommontea.veritechasse.Vanilla.RideableArmour;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.RideableArmour.Tags.CanEquipHarness;
import teacommontea.veritechasse.Vanilla.RideableArmour.Tags.HarnessColours;

public final class Harness {

    public static final int MAX_STACK_SIZE = 1;

    public static final boolean HAS_DURABILITY = false;
    public static final boolean DISPENSABLE = true;
    public static final boolean EQUIP_ON_INTERACT = true;
    public static final boolean CAN_BE_SHEARED = true;
    public static final boolean DAMAGE_ON_HURT = false;
    public static final boolean HAS_CAMERA_OVERLAY = true;

    public static final String SLOT_NAME = "body";

    public static final int PROTOCOL_MAJOR = 1;
    public static final int PROTOCOL_MINOR = 21;
    public static final int PROTOCOL_PATCH = 6;

    private Harness() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(PROTOCOL_MAJOR, PROTOCOL_MINOR, PROTOCOL_PATCH);
    }

    public static boolean is(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return HarnessColours.contains(stack.getType().name().toLowerCase(Locale.ROOT));
    }

    public static boolean canBeEquippedTo(String entityName, Protocol protocol) {
        return CanEquipHarness.contains(entityName, protocol);
    }

    public static int colourCount() {
        return HarnessColours.count();
    }
}

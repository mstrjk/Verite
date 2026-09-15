package teacommontea.veritechasse.vanilla.RideableArmour;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.ControlGate;
import teacommontea.veritechasse.vanilla.RideableArmour.Tags.CanEquipSaddle;

public final class Saddle {

    public static final String KEY = "saddle";

    public static final int MAX_STACK_SIZE = 1;

    public static final boolean HAS_DURABILITY = false;
    public static final boolean DISPENSABLE = true;
    public static final boolean EQUIP_ON_INTERACT = true;
    public static final boolean CAN_BE_SHEARED = true;
    public static final boolean DAMAGE_ON_HURT = false;

    public static final String SLOT_NAME = "saddle";

    public static final int SLOT_PROTOCOL_MAJOR = 1;
    public static final int SLOT_PROTOCOL_MINOR = 21;
    public static final int SLOT_PROTOCOL_PATCH = 5;

    private Saddle() {
    }

    public static boolean is(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getType().name().toLowerCase(Locale.ROOT).equals(KEY);
    }

    public static boolean occupiesEquipmentSlot(Protocol protocol) {
        return protocol.atLeast(SLOT_PROTOCOL_MAJOR, SLOT_PROTOCOL_MINOR, SLOT_PROTOCOL_PATCH);
    }

    public static boolean canBeEquippedTo(String entityName, Protocol protocol) {
        return CanEquipSaddle.contains(entityName, protocol);
    }

    public static ControlGate controlGate(String entityName) {
        if (entityName == null) {
            return ControlGate.NONE;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (name.equals("pig")) {
            return ControlGate.SADDLE_AND_CARROT_ON_A_STICK;
        }
        if (name.equals("strider")) {
            return ControlGate.SADDLE_AND_WARPED_FUNGUS_ON_A_STICK;
        }
        return ControlGate.SADDLE_ONLY;
    }

    public static boolean grantsControl(
            String entityName,
            boolean saddled,
            String heldItemName,
            Protocol protocol) {
        if (!canBeEquippedTo(entityName, protocol)) {
            return false;
        }
        return controlGate(entityName).satisfiedBy(saddled, heldItemName, false);
    }
}

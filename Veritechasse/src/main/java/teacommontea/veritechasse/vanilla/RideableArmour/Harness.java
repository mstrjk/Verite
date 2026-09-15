package teacommontea.veritechasse.vanilla.RideableArmour;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.ControlGate;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.MovementEnvelope;
import teacommontea.veritechasse.vanilla.RideableArmour.Tags.CanEquipHarness;
import teacommontea.veritechasse.vanilla.RideableArmour.Tags.HarnessColours;

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

    public static final double FLYING_SPEED_BASE = 0.05D;
    public static final float RIDDEN_INPUT_SCALE = 3.9F;
    public static final float JUMP_UPWARD_INPUT = 0.5F;
    public static final float BACKWARD_MULTIPLIER = -0.5F;
    public static final float TRAVEL_SPEED_RATIO = 5.0F / 3.0F;

    public static final int MAX_STILL_TIMEOUT = 10;
    public static final int STILL_TIMEOUT_ON_LOAD_GRACE_PERIOD = 60;

    public static final MovementEnvelope ENVELOPE =
        new MovementEnvelope(true, false, true, false, RIDDEN_INPUT_SCALE, BACKWARD_MULTIPLIER);

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

    public static ControlGate controlGate() {
        return ControlGate.HARNESS_AND_NOT_STILL;
    }

    public static boolean grantsControl(String entityName, boolean harnessed, boolean stillTimeout, Protocol protocol) {
        if (!canBeEquippedTo(entityName, protocol)) {
            return false;
        }
        return controlGate().satisfiedBy(harnessed, null, stillTimeout);
    }

    public static double maxRiddenSpeed(double flyingSpeedAttribute) {
        return flyingSpeedAttribute * RIDDEN_INPUT_SCALE;
    }

    public static double maxRiddenSpeed() {
        return maxRiddenSpeed(FLYING_SPEED_BASE);
    }

    public static double travelSpeed(double flyingSpeedAttribute) {
        return flyingSpeedAttribute * TRAVEL_SPEED_RATIO;
    }

    public static Reality riddenSpeedFrom(double flyingSpeedAttribute) {
        return Reality.of(maxRiddenSpeed(flyingSpeedAttribute));
    }

    public static Reality riddenSpeedFrom() {
        return Reality.of(maxRiddenSpeed());
    }
}

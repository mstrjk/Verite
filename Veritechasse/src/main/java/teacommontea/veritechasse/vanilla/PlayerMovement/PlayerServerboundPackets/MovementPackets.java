package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class MovementPackets {

    public static final String MOVE_PLAYER_POS = "pos";
    public static final String MOVE_PLAYER_POS_ROT = "pos_rot";
    public static final String MOVE_PLAYER_ROT = "rot";
    public static final String MOVE_PLAYER_STATUS_ONLY = "status_only";

    public static final String COMMAND_STOP_SLEEPING = "stop_sleeping";
    public static final String COMMAND_START_SPRINTING = "start_sprinting";
    public static final String COMMAND_STOP_SPRINTING = "stop_sprinting";
    public static final String COMMAND_START_RIDING_JUMP = "start_riding_jump";
    public static final String COMMAND_STOP_RIDING_JUMP = "stop_riding_jump";
    public static final String COMMAND_OPEN_INVENTORY = "open_inventory";
    public static final String COMMAND_START_FALL_FLYING = "start_fall_flying";
    public static final String COMMAND_PRESS_SHIFT_KEY = "press_shift_key";
    public static final String COMMAND_RELEASE_SHIFT_KEY = "release_shift_key";

    public static final int HORIZONTAL_COLLISION_PROTOCOL_MAJOR = 1;
    public static final int HORIZONTAL_COLLISION_PROTOCOL_MINOR = 21;
    public static final int HORIZONTAL_COLLISION_PROTOCOL_PATCH = 2;

    public static final int SHIFT_KEY_REMOVED_PROTOCOL_MAJOR = 1;
    public static final int SHIFT_KEY_REMOVED_PROTOCOL_MINOR = 21;
    public static final int SHIFT_KEY_REMOVED_PROTOCOL_PATCH = 6;

    private static final Set<String> POSITION_BEARING = Set.of(
        MOVE_PLAYER_POS,
        MOVE_PLAYER_POS_ROT);

    private static final Set<String> ROTATION_BEARING = Set.of(
        MOVE_PLAYER_POS_ROT,
        MOVE_PLAYER_ROT);

    private static final List<String> MOVEMENT_RELEVANT = List.of(
        PacketNames.MOVE_PLAYER,
        PacketNames.MOVE_VEHICLE,
        PacketNames.PLAYER_INPUT,
        PacketNames.PLAYER_COMMAND,
        PacketNames.PLAYER_ACTION,
        PacketNames.PLAYER_ABILITIES,
        PacketNames.ACCEPT_TELEPORTATION,
        PacketNames.PADDLE_BOAT,
        PacketNames.CLIENT_TICK_END);

    private MovementPackets() {
    }

    public static List<String> movementRelevant() {
        return MOVEMENT_RELEVANT;
    }

    public static boolean isMovementRelevant(String stableName) {
        if (stableName == null) {
            return false;
        }
        return MOVEMENT_RELEVANT.contains(stableName.toLowerCase(Locale.ROOT));
    }

    public static boolean carriesPosition(String variant) {
        return variant != null && POSITION_BEARING.contains(variant.toLowerCase(Locale.ROOT));
    }

    public static boolean carriesRotation(String variant) {
        return variant != null && ROTATION_BEARING.contains(variant.toLowerCase(Locale.ROOT));
    }

    public static boolean carriesHorizontalCollision(Protocol protocol) {
        return protocol.atLeast(
            HORIZONTAL_COLLISION_PROTOCOL_MAJOR,
            HORIZONTAL_COLLISION_PROTOCOL_MINOR,
            HORIZONTAL_COLLISION_PROTOCOL_PATCH);
    }

    public static boolean shiftKeyActionsExist(Protocol protocol) {
        return !protocol.atLeast(
            SHIFT_KEY_REMOVED_PROTOCOL_MAJOR,
            SHIFT_KEY_REMOVED_PROTOCOL_MINOR,
            SHIFT_KEY_REMOVED_PROTOCOL_PATCH);
    }

    public static boolean sneakIsReportedByCommand(Protocol protocol) {
        return shiftKeyActionsExist(protocol);
    }

    public static boolean sneakIsReportedByInput(Protocol protocol) {
        return !shiftKeyActionsExist(protocol);
    }

    public static boolean commandActionExists(String action, Protocol protocol) {
        if (action == null) {
            return false;
        }
        String key = action.toLowerCase(Locale.ROOT);
        if (COMMAND_PRESS_SHIFT_KEY.equals(key) || COMMAND_RELEASE_SHIFT_KEY.equals(key)) {
            return shiftKeyActionsExist(protocol);
        }
        return COMMAND_STOP_SLEEPING.equals(key)
            || COMMAND_START_SPRINTING.equals(key)
            || COMMAND_STOP_SPRINTING.equals(key)
            || COMMAND_START_RIDING_JUMP.equals(key)
            || COMMAND_STOP_RIDING_JUMP.equals(key)
            || COMMAND_OPEN_INVENTORY.equals(key)
            || COMMAND_START_FALL_FLYING.equals(key);
    }

    public static boolean available(String stableName, Protocol protocol) {
        return isMovementRelevant(stableName) && PacketRegistry.exists(stableName, protocol);
    }
}

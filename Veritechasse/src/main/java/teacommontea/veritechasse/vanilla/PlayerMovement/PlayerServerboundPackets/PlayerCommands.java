package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets;

import java.util.List;
import java.util.Locale;

import teacommontea.veritechasse.vanilla.Protocol;

public final class PlayerCommands {

    public static final String PRESS_SHIFT_KEY = "press_shift_key";
    public static final String RELEASE_SHIFT_KEY = "release_shift_key";
    public static final String STOP_SLEEPING = "stop_sleeping";
    public static final String START_SPRINTING = "start_sprinting";
    public static final String STOP_SPRINTING = "stop_sprinting";
    public static final String START_RIDING_JUMP = "start_riding_jump";
    public static final String STOP_RIDING_JUMP = "stop_riding_jump";
    public static final String OPEN_INVENTORY = "open_inventory";
    public static final String START_FALL_FLYING = "start_fall_flying";

    public static final int SHIFT_KEY_REMOVED_PROTOCOL_MAJOR = 1;
    public static final int SHIFT_KEY_REMOVED_PROTOCOL_MINOR = 21;
    public static final int SHIFT_KEY_REMOVED_PROTOCOL_PATCH = 6;

    private static final List<String> MODERN = List.of(
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        START_RIDING_JUMP,
        STOP_RIDING_JUMP,
        OPEN_INVENTORY,
        START_FALL_FLYING);

    private static final List<String> LEGACY = List.of(
        PRESS_SHIFT_KEY,
        RELEASE_SHIFT_KEY,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        START_RIDING_JUMP,
        STOP_RIDING_JUMP,
        OPEN_INVENTORY,
        START_FALL_FLYING);

    private PlayerCommands() {
    }

    public static String normalise(String action) {
        return action == null ? null : action.toLowerCase(Locale.ROOT);
    }

    public static boolean shiftKeyActionsExist(Protocol protocol) {
        return !protocol.atLeast(
            SHIFT_KEY_REMOVED_PROTOCOL_MAJOR,
            SHIFT_KEY_REMOVED_PROTOCOL_MINOR,
            SHIFT_KEY_REMOVED_PROTOCOL_PATCH);
    }

    public static List<String> available(Protocol protocol) {
        return shiftKeyActionsExist(protocol) ? LEGACY : MODERN;
    }

    public static boolean exists(String action, Protocol protocol) {
        String key = normalise(action);
        if (key == null) {
            return false;
        }
        if (PRESS_SHIFT_KEY.equals(key) || RELEASE_SHIFT_KEY.equals(key)) {
            return shiftKeyActionsExist(protocol);
        }
        return MODERN.contains(key);
    }

    public static boolean setsSprinting(String action) {
        return START_SPRINTING.equals(normalise(action));
    }

    public static boolean clearsSprinting(String action) {
        return STOP_SPRINTING.equals(normalise(action));
    }

    public static boolean setsSneaking(String action) {
        return PRESS_SHIFT_KEY.equals(normalise(action));
    }

    public static boolean clearsSneaking(String action) {
        return RELEASE_SHIFT_KEY.equals(normalise(action));
    }

    public static boolean affectsSprintState(String action) {
        return setsSprinting(action) || clearsSprinting(action);
    }

    public static boolean affectsSneakState(String action) {
        return setsSneaking(action) || clearsSneaking(action);
    }

    public static boolean affectsMovement(String action) {
        String key = normalise(action);
        if (key == null) {
            return false;
        }
        return affectsSprintState(key)
            || affectsSneakState(key)
            || START_FALL_FLYING.equals(key)
            || START_RIDING_JUMP.equals(key)
            || STOP_RIDING_JUMP.equals(key);
    }

    public static boolean startsGliding(String action) {
        return START_FALL_FLYING.equals(normalise(action));
    }

    public static boolean affectsVehicle(String action) {
        String key = normalise(action);
        return START_RIDING_JUMP.equals(key) || STOP_RIDING_JUMP.equals(key);
    }
}

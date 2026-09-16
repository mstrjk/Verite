package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets;

import java.util.List;
import java.util.Locale;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class PlayerActions {

    public static final String START_DESTROY_BLOCK = "start_destroy_block";
    public static final String ABORT_DESTROY_BLOCK = "abort_destroy_block";
    public static final String STOP_DESTROY_BLOCK = "stop_destroy_block";
    public static final String DROP_ALL_ITEMS = "drop_all_items";
    public static final String DROP_ITEM = "drop_item";
    public static final String RELEASE_USE_ITEM = "release_use_item";
    public static final String SWAP_ITEM_WITH_OFFHAND = "swap_item_with_offhand";
    public static final String STAB = "stab";

    public static final int STAB_PROTOCOL_MAJOR = 1;
    public static final int STAB_PROTOCOL_MINOR = 21;
    public static final int STAB_PROTOCOL_PATCH = 11;

    private static final List<String> ALWAYS_PRESENT = List.of(
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND);

    private static final List<String> DIGGING = List.of(
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK);

    private PlayerActions() {
    }

    public static String normalise(String action) {
        return action == null ? null : action.toLowerCase(Locale.ROOT);
    }

    public static boolean stabExists(Protocol protocol) {
        return protocol.atLeast(
            STAB_PROTOCOL_MAJOR,
            STAB_PROTOCOL_MINOR,
            STAB_PROTOCOL_PATCH);
    }

    public static List<String> available(Protocol protocol) {
        if (!stabExists(protocol)) {
            return ALWAYS_PRESENT;
        }
        return List.of(
            START_DESTROY_BLOCK,
            ABORT_DESTROY_BLOCK,
            STOP_DESTROY_BLOCK,
            DROP_ALL_ITEMS,
            DROP_ITEM,
            RELEASE_USE_ITEM,
            SWAP_ITEM_WITH_OFFHAND,
            STAB);
    }

    public static boolean exists(String action, Protocol protocol) {
        String key = normalise(action);
        if (key == null) {
            return false;
        }
        if (STAB.equals(key)) {
            return stabExists(protocol);
        }
        return ALWAYS_PRESENT.contains(key);
    }

    public static boolean isDigging(String action) {
        String key = normalise(action);
        return key != null && DIGGING.contains(key);
    }

    public static boolean beginsDigging(String action) {
        return START_DESTROY_BLOCK.equals(normalise(action));
    }

    public static boolean endsDigging(String action) {
        String key = normalise(action);
        return ABORT_DESTROY_BLOCK.equals(key) || STOP_DESTROY_BLOCK.equals(key);
    }

    public static boolean isItemDrop(String action) {
        String key = normalise(action);
        return DROP_ITEM.equals(key) || DROP_ALL_ITEMS.equals(key);
    }

    public static boolean releasesUse(String action) {
        return RELEASE_USE_ITEM.equals(normalise(action));
    }

    public static boolean requiresBlockPosition(String action) {
        return isDigging(normalise(action));
    }
}

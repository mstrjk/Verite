package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerTeleport;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Protocol;

public final class TeleportCauses {

    public static final String ENDER_PEARL = "ender_pearl";
    public static final String CHORUS_FRUIT = "chorus_fruit";
    public static final String CONSUMABLE_EFFECT = "consumable_effect";
    public static final String NETHER_PORTAL = "nether_portal";
    public static final String END_PORTAL = "end_portal";
    public static final String END_GATEWAY = "end_gateway";
    public static final String SPECTATE = "spectate";
    public static final String DISMOUNT = "dismount";
    public static final String EXIT_BED = "exit_bed";
    public static final String COMMAND = "command";
    public static final String PLUGIN = "plugin";
    public static final String UNKNOWN = "unknown";

    public static final int DISMOUNT_PROTOCOL_MAJOR = 1;
    public static final int DISMOUNT_PROTOCOL_MINOR = 20;
    public static final int DISMOUNT_PROTOCOL_PATCH = 2;

    public static final int CONSUMABLE_EFFECT_PROTOCOL_MAJOR = 1;
    public static final int CONSUMABLE_EFFECT_PROTOCOL_MINOR = 21;
    public static final int CONSUMABLE_EFFECT_PROTOCOL_PATCH = 5;

    private static final Set<String> VANILLA_CAUSES = Set.of(
        ENDER_PEARL,
        CHORUS_FRUIT,
        CONSUMABLE_EFFECT,
        NETHER_PORTAL,
        END_PORTAL,
        END_GATEWAY,
        SPECTATE,
        DISMOUNT,
        EXIT_BED);

    private static final Set<String> EXTERNAL_CAUSES = Set.of(
        COMMAND,
        PLUGIN);

    private TeleportCauses() {
    }

    public static String normalise(String cause) {
        return cause == null ? UNKNOWN : cause.toLowerCase(Locale.ROOT);
    }

    public static boolean dismountCausesExist(Protocol protocol) {
        return protocol.atLeast(
            DISMOUNT_PROTOCOL_MAJOR,
            DISMOUNT_PROTOCOL_MINOR,
            DISMOUNT_PROTOCOL_PATCH);
    }

    public static boolean consumableEffectCauseExists(Protocol protocol) {
        return protocol.atLeast(
            CONSUMABLE_EFFECT_PROTOCOL_MAJOR,
            CONSUMABLE_EFFECT_PROTOCOL_MINOR,
            CONSUMABLE_EFFECT_PROTOCOL_PATCH);
    }

    public static boolean exists(String cause, Protocol protocol) {
        String key = normalise(cause);
        if (DISMOUNT.equals(key) || EXIT_BED.equals(key)) {
            return dismountCausesExist(protocol);
        }
        if (CONSUMABLE_EFFECT.equals(key)) {
            return consumableEffectCauseExists(protocol);
        }
        return VANILLA_CAUSES.contains(key) || EXTERNAL_CAUSES.contains(key) || UNKNOWN.equals(key);
    }

    public static boolean isVanilla(String cause) {
        return VANILLA_CAUSES.contains(normalise(cause));
    }

    public static boolean isExternal(String cause) {
        return EXTERNAL_CAUSES.contains(normalise(cause));
    }

    public static boolean isUnknown(String cause) {
        return UNKNOWN.equals(normalise(cause));
    }

    public static boolean chorusFruitReportsAs(String cause, Protocol protocol) {
        String key = normalise(cause);
        if (consumableEffectCauseExists(protocol)) {
            return CONSUMABLE_EFFECT.equals(key) || CHORUS_FRUIT.equals(key);
        }
        return CHORUS_FRUIT.equals(key);
    }
}

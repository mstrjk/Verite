package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class LandingBlocks {

    public static final float DEFAULT_DAMAGE_MODIFIER = 1.0F;

    public static final String HAY_BLOCK = "hay_block";
    public static final String HONEY_BLOCK = "honey_block";
    public static final String SLIME_BLOCK = "slime_block";
    public static final String POINTED_DRIPSTONE = "pointed_dripstone";
    public static final String BED = "bed";

    public static final float HAY_BLOCK_MODIFIER = 0.2F;
    public static final float HONEY_BLOCK_MODIFIER = 0.2F;
    public static final float SLIME_BLOCK_MODIFIER = 0.0F;
    public static final float POINTED_DRIPSTONE_MODIFIER = 2.0F;

    public static final double BED_DISTANCE_SCALE = 0.5D;

    public static final double POINTED_DRIPSTONE_ADDED_DISTANCE = 2.5D;
    public static final double LEGACY_POINTED_DRIPSTONE_ADDED_DISTANCE = 2.0D;

    public static final int DRIPSTONE_DISTANCE_PROTOCOL_MAJOR = 1;
    public static final int DRIPSTONE_DISTANCE_PROTOCOL_MINOR = 21;
    public static final int DRIPSTONE_DISTANCE_PROTOCOL_PATCH = 5;

    private static final Map<String, Float> DAMAGE_MODIFIERS = Map.of(
        HAY_BLOCK, Float.valueOf(HAY_BLOCK_MODIFIER),
        HONEY_BLOCK, Float.valueOf(HONEY_BLOCK_MODIFIER),
        SLIME_BLOCK, Float.valueOf(SLIME_BLOCK_MODIFIER),
        POINTED_DRIPSTONE, Float.valueOf(POINTED_DRIPSTONE_MODIFIER));

    private LandingBlocks() {
    }

    public static String normalise(String blockName) {
        if (blockName == null) {
            return "";
        }
        String lower = blockName.toLowerCase(Locale.ROOT);
        return lower.endsWith("_bed") ? BED : lower;
    }

    public static float damageModifierOf(String blockName) {
        Float found = DAMAGE_MODIFIERS.get(normalise(blockName));
        return found == null ? DEFAULT_DAMAGE_MODIFIER : found.floatValue();
    }

    public static boolean negatesFallDamage(String blockName) {
        return damageModifierOf(blockName) == SLIME_BLOCK_MODIFIER;
    }

    public static boolean amplifiesFallDamage(String blockName) {
        return damageModifierOf(blockName) > DEFAULT_DAMAGE_MODIFIER;
    }

    public static boolean reducesFallDamage(String blockName) {
        float modifier = damageModifierOf(blockName);
        return modifier > SLIME_BLOCK_MODIFIER && modifier < DEFAULT_DAMAGE_MODIFIER;
    }

    public static boolean halvesDistance(String blockName) {
        return BED.equals(normalise(blockName));
    }

    public static boolean dripstoneUsesExtendedDistance(Protocol protocol) {
        return protocol.atLeast(
            DRIPSTONE_DISTANCE_PROTOCOL_MAJOR,
            DRIPSTONE_DISTANCE_PROTOCOL_MINOR,
            DRIPSTONE_DISTANCE_PROTOCOL_PATCH);
    }

    public static double addedDistance(String blockName, Protocol protocol) {
        if (!POINTED_DRIPSTONE.equals(normalise(blockName))) {
            return 0.0D;
        }
        return dripstoneUsesExtendedDistance(protocol)
            ? POINTED_DRIPSTONE_ADDED_DISTANCE
            : LEGACY_POINTED_DRIPSTONE_ADDED_DISTANCE;
    }

    public static double effectiveDistance(String blockName, double fallDistance, Protocol protocol) {
        double scaled = halvesDistance(blockName)
            ? fallDistance * BED_DISTANCE_SCALE
            : fallDistance;
        return scaled + addedDistance(blockName, protocol);
    }

    public static Reality modifierFrom(String blockName) {
        return Reality.of(damageModifierOf(blockName));
    }
}

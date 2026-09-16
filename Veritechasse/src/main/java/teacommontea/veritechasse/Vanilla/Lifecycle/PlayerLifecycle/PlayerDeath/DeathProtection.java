package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerDeath;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class DeathProtection {

    public static final String KEY = "death_protection";

    public static final int DATA_DRIVEN_MAJOR = 1;
    public static final int DATA_DRIVEN_MINOR = 21;
    public static final int DATA_DRIVEN_PATCH = 2;

    public static final float HEALTH_AFTER_PROTECTION = 1.0F;

    public static final int LEGACY_REGENERATION_TICKS = 900;
    public static final int LEGACY_REGENERATION_AMPLIFIER = 1;

    public static final int LEGACY_ABSORPTION_TICKS = 100;
    public static final int LEGACY_ABSORPTION_AMPLIFIER = 1;

    public static final int LEGACY_FIRE_RESISTANCE_TICKS = 800;
    public static final int LEGACY_FIRE_RESISTANCE_AMPLIFIER = 0;

    public static final String TOTEM_OF_UNDYING = "totem_of_undying";

    private DeathProtection() {
    }

    public static boolean isDataDriven(Protocol protocol) {
        return protocol.atLeast(
            DATA_DRIVEN_MAJOR, DATA_DRIVEN_MINOR, DATA_DRIVEN_PATCH);
    }

    public static boolean bypassedByDamage(boolean bypassesInvulnerability) {
        return bypassesInvulnerability;
    }

    public static boolean heldInCheckedSlot(boolean mainHand, boolean offHand) {
        return mainHand || offHand;
    }

    public static boolean mainHandTakesPriority() {
        return true;
    }

    public static boolean protects(
            boolean bypassesInvulnerability,
            boolean mainHandHasProtection,
            boolean offHandHasProtection) {
        if (bypassedByDamage(bypassesInvulnerability)) {
            return false;
        }
        return heldInCheckedSlot(mainHandHasProtection, offHandHasProtection);
    }

    public static float healthAfterProtection() {
        return HEALTH_AFTER_PROTECTION;
    }

    public static boolean clearsExistingEffects(Protocol protocol) {
        return !isDataDriven(protocol);
    }

    public static boolean consumesOneItem() {
        return true;
    }

    public static boolean armourSlotProtects() {
        return false;
    }
}

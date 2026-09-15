package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerGlide;

import teacommontea.veritechasse.vanilla.Potions.Levitation;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;

public final class GlideGate {

    public static final String ELYTRA = "elytra";

    public static final int SHARED_FLAG = 7;

    public static final int DURABILITY_TICK_INTERVAL = 10;
    public static final int DURABILITY_EVERY_OTHER_INTERVAL = 2;
    public static final int DURABILITY_PER_HIT = 1;

    public static final int GLIDER_COMPONENT_PROTOCOL_MAJOR = 1;
    public static final int GLIDER_COMPONENT_PROTOCOL_MINOR = 21;
    public static final int GLIDER_COMPONENT_PROTOCOL_PATCH = 2;

    private GlideGate() {
    }

    public static boolean gliderIsComponentDriven(Protocol protocol) {
        return protocol.atLeast(
            GLIDER_COMPONENT_PROTOCOL_MAJOR,
            GLIDER_COMPONENT_PROTOCOL_MINOR,
            GLIDER_COMPONENT_PROTOCOL_PATCH);
    }

    public static boolean chestSlotOnly(Protocol protocol) {
        return !gliderIsComponentDriven(protocol);
    }

    public static boolean canGlide(
            boolean onGround,
            boolean passenger,
            ActiveEffects effects,
            boolean hasWorkingGlider) {
        if (onGround || passenger) {
            return false;
        }
        if (effects != null && effects.has(Levitation.KEY)) {
            return false;
        }
        return hasWorkingGlider;
    }

    public static boolean gliderIsUsable(
            boolean hasGliderComponent,
            boolean equippedInMatchingSlot,
            boolean nextDamageWillBreak) {
        if (!hasGliderComponent || !equippedInMatchingSlot) {
            return false;
        }
        return !nextDamageWillBreak;
    }

    public static boolean legacyElytraIsUsable(String itemKey, int damage, int maxDamage) {
        if (itemKey == null || !ELYTRA.equals(itemKey)) {
            return false;
        }
        return maxDamage - damage > DURABILITY_PER_HIT;
    }

    public static boolean flyingSuppressesGlide(boolean flying) {
        return flying;
    }

    public static boolean durabilityTickAt(int fallFlyTicks) {
        int next = fallFlyTicks + 1;
        if (next % DURABILITY_TICK_INTERVAL != 0) {
            return false;
        }
        return next / DURABILITY_TICK_INTERVAL % DURABILITY_EVERY_OTHER_INTERVAL == 0;
    }

    public static int durabilityCostOver(int glidingTicks) {
        int hits = 0;
        for (int tick = 0; tick < glidingTicks; tick++) {
            if (durabilityTickAt(tick)) {
                hits = hits + DURABILITY_PER_HIT;
            }
        }
        return hits;
    }

    public static int ticksPerDurabilityPoint() {
        return DURABILITY_TICK_INTERVAL * DURABILITY_EVERY_OTHER_INTERVAL;
    }
}

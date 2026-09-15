package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.vanilla.Protocol;

public final class Invisibility {

    public static final String KEY = "invisibility";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final double WAYPOINT_TRANSMIT_RANGE_PER_AMPLIFIER = -1.0D;
    public static final Operation OPERATION = Operation.ADD_MULTIPLIED_TOTAL;

    private Invisibility() {
    }

    public static boolean setsInvisible() {
        return true;
    }

    public static boolean hidesWaypoint(Protocol protocol) {
        return protocol.atLeast(1, 21, 6);
    }

    public static Modifier waypointModifier(int amplifier) {
        return new Modifier(WAYPOINT_TRANSMIT_RANGE_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double waypointTransmitRange(int amplifier, Protocol protocol) {
        if (!hidesWaypoint(protocol)) {
            return Attributes.WAYPOINT_TRANSMIT_RANGE.base();
        }
        return AttributePipeline.resolve(Attributes.WAYPOINT_TRANSMIT_RANGE, waypointModifier(amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}

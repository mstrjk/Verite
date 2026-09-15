package teacommontea.veritechasse.vanilla.Tools.Utility.Support;

import teacommontea.veritechasse.vanilla.Protocol;

public final class Steering {

    public static final int BOOST_MIN_TICKS = 140;
    public static final int BOOST_RANGE = 841;
    public static final int BOOST_MAX_TICKS = 980;

    public static final String CONVERTS_TO = "fishing_rod";

    private Steering() {
    }

    public static boolean convertsOnBreak(Protocol protocol) {
        return protocol.atLeast(1, 21, 0);
    }

    public static int boostDuration(int roll) {
        return BOOST_MIN_TICKS + roll;
    }

    public static int minBoostTicks() {
        return BOOST_MIN_TICKS;
    }

    public static int maxBoostTicks() {
        return BOOST_MAX_TICKS;
    }

    public static boolean canBoost(boolean riding, boolean correctVehicle, boolean alreadyBoosting) {
        return riding && correctVehicle && !alreadyBoosting;
    }
}

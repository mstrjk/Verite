package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientToServerForgiveness;

public final class GroundProbe {

    public static final String KEY = "ground_probe";

    public static final double INFLATE = 0.0625D;

    public static final double DOWNWARD_REACH = 0.55D;

    private GroundProbe() {
    }

    public static double probeMinX(double boxMinX) {
        return boxMinX - INFLATE;
    }

    public static double probeMaxX(double boxMaxX) {
        return boxMaxX + INFLATE;
    }

    public static double probeMinY(double boxMinY) {
        return boxMinY - INFLATE - DOWNWARD_REACH;
    }

    public static double probeMaxY(double boxMaxY) {
        return boxMaxY + INFLATE;
    }

    public static double probeMinZ(double boxMinZ) {
        return boxMinZ - INFLATE;
    }

    public static double probeMaxZ(double boxMaxZ) {
        return boxMaxZ + INFLATE;
    }

    public static double totalDownwardReach() {
        return INFLATE + DOWNWARD_REACH;
    }

    public static boolean noBlocksAround(boolean allProbedStatesAreAir) {
        return allProbedStatesAreAir;
    }

    public static boolean probeIncludesNonSolidBlocks() {
        return true;
    }

    public static boolean waterCountsAsBlock() {
        return true;
    }

    public static boolean probeExpandsUpward() {
        return true;
    }

    public static boolean inflateAffectsAllAxes() {
        return true;
    }

    public static boolean expandTowardsAffectsOnlyDownward() {
        return true;
    }
}

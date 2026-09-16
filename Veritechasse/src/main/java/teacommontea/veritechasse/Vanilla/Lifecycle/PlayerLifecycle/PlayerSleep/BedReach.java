package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerSleep;

import teacommontea.veritechasse.Vanilla.Reality;

public final class BedReach {

    public static final String KEY = "bed_reach";

    public static final double HORIZONTAL_REACH = 3.0D;

    public static final double VERTICAL_REACH = 2.0D;

    public static final double BED_CENTRE_OFFSET = 0.5D;

    private BedReach() {
    }

    public static boolean withinAxisReach(double playerAxis, double bedCentreAxis, double reach) {
        return Math.abs(playerAxis - bedCentreAxis) <= reach;
    }

    public static boolean reachesBedBlock(
            double playerX,
            double playerY,
            double playerZ,
            double bedCentreX,
            double bedCentreY,
            double bedCentreZ) {
        return withinAxisReach(playerX, bedCentreX, HORIZONTAL_REACH)
            && withinAxisReach(playerY, bedCentreY, VERTICAL_REACH)
            && withinAxisReach(playerZ, bedCentreZ, HORIZONTAL_REACH);
    }

    public static boolean inRange(
            double playerX,
            double playerY,
            double playerZ,
            double headCentreX,
            double headCentreY,
            double headCentreZ,
            double footCentreX,
            double footCentreY,
            double footCentreZ) {
        return reachesBedBlock(playerX, playerY, playerZ,
                headCentreX, headCentreY, headCentreZ)
            || reachesBedBlock(playerX, playerY, playerZ,
                footCentreX, footCentreY, footCentreZ);
    }

    public static double bottomCentreOf(int blockCoordinate) {
        return (double) blockCoordinate + BED_CENTRE_OFFSET;
    }

    public static double bottomOf(int blockCoordinate) {
        return (double) blockCoordinate;
    }

    public static boolean bothHalvesChecked() {
        return true;
    }

    public static Reality horizontalReach() {
        return Reality.of(HORIZONTAL_REACH);
    }

    public static Reality verticalReach() {
        return Reality.of(VERTICAL_REACH);
    }
}

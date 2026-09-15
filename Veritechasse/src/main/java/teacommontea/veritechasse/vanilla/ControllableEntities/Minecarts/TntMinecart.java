package teacommontea.veritechasse.vanilla.ControllableEntities.Minecarts;

import teacommontea.veritechasse.vanilla.Reality;

public final class TntMinecart {

    public static final String KEY = "tnt_minecart";

    public static final int NO_FUSE = -1;
    public static final int ACTIVATOR_FUSE_TICKS = 80;

    public static final int RANDOM_FUSE_BOUND = 20;
    public static final int RANDOM_FUSE_MINIMUM = 0;
    public static final int RANDOM_FUSE_MAXIMUM = 2 * (RANDOM_FUSE_BOUND - 1);

    public static final double DEFAULT_EXPLOSION_POWER_BASE = 4.0D;
    public static final double EXPLOSION_SPEED_SCALE = 1.5D;
    public static final double EXPLOSION_SPEED_CAP = 5.0D;

    private TntMinecart() {
    }

    public static double maxExplosionPower(double explosionPowerBase, double explosionSpeedFactor, double speed) {
        double cappedSpeed = speed > EXPLOSION_SPEED_CAP ? EXPLOSION_SPEED_CAP : speed;
        return explosionPowerBase + explosionSpeedFactor * EXPLOSION_SPEED_SCALE * cappedSpeed;
    }

    public static Reality explosionPowerFrom(double explosionPowerBase, double explosionSpeedFactor, double speed) {
        return Reality.of(maxExplosionPower(explosionPowerBase, explosionSpeedFactor, speed));
    }

    public static boolean primed(int fuse) {
        return fuse >= 0;
    }

    public static int fuseAfterTick(int fuse) {
        if (fuse <= 0) {
            return fuse;
        }
        return fuse - 1;
    }

    public static boolean explodesThisTick(int fuse) {
        return fuse == 0;
    }

    public static boolean randomFuseIsLegal(int fuse) {
        return fuse >= RANDOM_FUSE_MINIMUM && fuse <= RANDOM_FUSE_MAXIMUM;
    }

    public static Reality randomFuseCeiling() {
        return Reality.of(RANDOM_FUSE_MAXIMUM);
    }
}

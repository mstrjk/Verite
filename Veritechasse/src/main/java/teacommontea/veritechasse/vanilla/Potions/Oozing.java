package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class Oozing {

    public static final String KEY = "oozing";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final int SLIMES_SPAWNED = 2;
    public static final int SLIME_SIZE = 2;
    public static final double RADIUS_TO_CHECK_SLIMES = 2.0D;
    public static final double SPAWN_Y_OFFSET = 0.5D;

    private Oozing() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static boolean triggersOnDeath() {
        return true;
    }

    public static int spawnCount(int maxEntityCramming, int nearbySlimes) {
        if (maxEntityCramming < 1) {
            return SLIMES_SPAWNED;
        }
        int room = maxEntityCramming - nearbySlimes;
        if (room < 0) {
            return 0;
        }
        return room > SLIMES_SPAWNED ? SLIMES_SPAWNED : room;
    }

    public static Reality maxSlimes() {
        return Reality.of(SLIMES_SPAWNED);
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}

package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerSleep;

public final class SleepGate {

    public static final String KEY = "sleep_gate";

    public static final String NOT_POSSIBLE_HERE = "not_possible_here";
    public static final String NOT_POSSIBLE_NOW = "not_possible_now";
    public static final String TOO_FAR_AWAY = "too_far_away";
    public static final String OBSTRUCTED = "obstructed";
    public static final String NOT_SAFE = "not_safe";
    public static final String OTHER_PROBLEM = "other_problem";

    public static final double MONSTER_HORIZONTAL_RANGE = 8.0D;

    public static final double MONSTER_VERTICAL_RANGE = 5.0D;

    public static final int DEFAULT_SLEEPING_PERCENTAGE = 100;

    private SleepGate() {
    }

    public static boolean alreadySleepingOrDead(boolean sleeping, boolean alive) {
        return sleeping || !alive;
    }

    public static boolean monsterCheckApplies(boolean creative) {
        return !creative;
    }

    public static boolean monsterIsInRange(
            double monsterX,
            double monsterY,
            double monsterZ,
            double bedCentreX,
            double bedCentreY,
            double bedCentreZ) {
        return Math.abs(monsterX - bedCentreX) <= MONSTER_HORIZONTAL_RANGE
            && Math.abs(monsterY - bedCentreY) <= MONSTER_VERTICAL_RANGE
            && Math.abs(monsterZ - bedCentreZ) <= MONSTER_HORIZONTAL_RANGE;
    }

    public static String problem(
            boolean sleeping,
            boolean alive,
            boolean canSleep,
            boolean canSetSpawn,
            boolean inRange,
            boolean blocked,
            boolean creative,
            boolean monstersNearby) {
        if (alreadySleepingOrDead(sleeping, alive)) {
            return OTHER_PROBLEM;
        }
        if (!canSetSpawn && !canSleep) {
            return NOT_POSSIBLE_HERE;
        }
        if (!inRange) {
            return TOO_FAR_AWAY;
        }
        if (blocked) {
            return OBSTRUCTED;
        }
        if (!canSleep) {
            return NOT_POSSIBLE_NOW;
        }
        if (monsterCheckApplies(creative) && monstersNearby) {
            return NOT_SAFE;
        }
        return null;
    }

    public static boolean sleepPermitted(
            boolean sleeping,
            boolean alive,
            boolean canSleep,
            boolean canSetSpawn,
            boolean inRange,
            boolean blocked,
            boolean creative,
            boolean monstersNearby) {
        return problem(sleeping, alive, canSleep, canSetSpawn,
            inRange, blocked, creative, monstersNearby) == null;
    }

    public static boolean spawnIsSetEvenWhenSleepFails(
            boolean canSetSpawn, boolean inRange, boolean blocked) {
        return canSetSpawn && inRange && !blocked;
    }

    public static int sleepersNeeded(int activePlayers, int percentage) {
        float scaled = (float) (activePlayers * percentage) / 100.0F;
        return Math.max(1, (int) Math.ceil((double) scaled));
    }

    public static boolean enoughSleeping(
            int sleepingPlayers, int activePlayers, int percentage) {
        return sleepingPlayers >= sleepersNeeded(activePlayers, percentage);
    }

    public static boolean enoughDeepSleeping(
            int deepSleepers, int activePlayers, int percentage) {
        return deepSleepers >= sleepersNeeded(activePlayers, percentage);
    }

    public static boolean nightSkips(
            int sleepingPlayers, int deepSleepers, int activePlayers, int percentage) {
        return enoughSleeping(sleepingPlayers, activePlayers, percentage)
            && enoughDeepSleeping(deepSleepers, activePlayers, percentage);
    }

    public static boolean nightSkipPossible(int percentage) {
        return percentage <= DEFAULT_SLEEPING_PERCENTAGE;
    }
}

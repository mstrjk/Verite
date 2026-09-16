package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerDeath;

import teacommontea.veritechasse.Vanilla.Reality;

public final class DeathTimer {

    public static final String KEY = "death_timer";

    public static final int DEATH_DURATION = 20;

    public static final double DEAD_HEALTH_THRESHOLD = 0.0D;

    private DeathTimer() {
    }

    public static boolean isDeadOrDying(double health) {
        return health <= DEAD_HEALTH_THRESHOLD;
    }

    public static int afterTick(int deathTime) {
        return deathTime + 1;
    }

    public static boolean removalDue(int deathTime) {
        return deathTime >= DEATH_DURATION;
    }

    public static int ticksUntilRemoval(int deathTime) {
        int remaining = DEATH_DURATION - deathTime;
        return remaining <= 0 ? 0 : remaining;
    }

    public static boolean respawnIsGatedOnDeathTime() {
        return false;
    }

    public static boolean movementIsEvaluated(double health) {
        return !isDeadOrDying(health);
    }

    public static Reality removalTick() {
        return Reality.of(DEATH_DURATION);
    }
}

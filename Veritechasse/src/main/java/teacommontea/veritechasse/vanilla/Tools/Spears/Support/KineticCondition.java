package teacommontea.veritechasse.vanilla.Tools.Spears.Support;

public final class KineticCondition {

    private final int maxDurationTicks;
    private final float minSpeed;
    private final float minRelativeSpeed;

    public KineticCondition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
        this.maxDurationTicks = maxDurationTicks;
        this.minSpeed = minSpeed;
        this.minRelativeSpeed = minRelativeSpeed;
    }

    public static KineticCondition ofAttackerSpeed(float untilSeconds, float minAttackerSpeed) {
        return new KineticCondition((int) (untilSeconds * 20.0F), minAttackerSpeed, 0.0F);
    }

    public static KineticCondition ofRelativeSpeed(float untilSeconds, float minRelativeSpeed) {
        return new KineticCondition((int) (untilSeconds * 20.0F), 0.0F, minRelativeSpeed);
    }

    public int maxDurationTicks() {
        return this.maxDurationTicks;
    }

    public float minSpeed() {
        return this.minSpeed;
    }

    public float minRelativeSpeed() {
        return this.minRelativeSpeed;
    }

    public boolean test(int ticksUsed, double attackerSpeed, double relativeSpeed, double entityFactor) {
        if (ticksUsed > this.maxDurationTicks) {
            return false;
        }
        if (attackerSpeed < (double) this.minSpeed * entityFactor) {
            return false;
        }
        return relativeSpeed >= (double) this.minRelativeSpeed * entityFactor;
    }
}

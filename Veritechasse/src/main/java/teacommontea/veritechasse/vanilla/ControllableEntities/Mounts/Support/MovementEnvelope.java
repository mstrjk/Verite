package teacommontea.veritechasse.Vanilla.ControllableEntities.Mounts.Support;

public final class MovementEnvelope {

    private final boolean steerable;
    private final boolean canJump;
    private final boolean canFly;
    private final boolean canDash;
    private final float speedMultiplier;
    private final float backwardMultiplier;

    public MovementEnvelope(
            boolean steerable,
            boolean canJump,
            boolean canFly,
            boolean canDash,
            float speedMultiplier,
            float backwardMultiplier) {
        this.steerable = steerable;
        this.canJump = canJump;
        this.canFly = canFly;
        this.canDash = canDash;
        this.speedMultiplier = speedMultiplier;
        this.backwardMultiplier = backwardMultiplier;
    }

    public boolean steerable() {
        return this.steerable;
    }

    public boolean canJump() {
        return this.canJump;
    }

    public boolean canFly() {
        return this.canFly;
    }

    public boolean canDash() {
        return this.canDash;
    }

    public float speedMultiplier() {
        return this.speedMultiplier;
    }

    public float backwardMultiplier() {
        return this.backwardMultiplier;
    }

    public double maxHorizontalSpeed(double baseSpeed) {
        return baseSpeed * this.speedMultiplier;
    }

    public double maxHorizontalSpeed(double baseSpeed, float boostFactor) {
        return baseSpeed * this.speedMultiplier * boostFactor;
    }

    public boolean permitsVerticalInput() {
        return this.canFly || this.canJump;
    }
}

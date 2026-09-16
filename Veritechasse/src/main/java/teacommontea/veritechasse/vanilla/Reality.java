package teacommontea.veritechasse.Vanilla;

public final class Reality {

    public static final Reality NONE = new Reality(0.0D, false);

    private final double bound;
    private final boolean possible;

    private Reality(double bound, boolean possible) {
        this.bound = bound;
        this.possible = possible;
    }

    public static Reality of(double bound) {
        return new Reality(bound, true);
    }

    public static Reality impossible() {
        return NONE;
    }

    public double bound() {
        return this.bound;
    }

    public boolean possible() {
        return this.possible;
    }

    public boolean permits(double observed) {
        return this.possible && observed <= this.bound;
    }

    public boolean exceededBy(double observed) {
        return !this.permits(observed);
    }

    public double excessOf(double observed) {
        if (this.permits(observed)) {
            return 0.0D;
        }
        return observed - this.bound;
    }

    public Reality withTolerance(double tolerance) {
        if (!this.possible) {
            return this;
        }
        return new Reality(this.bound + tolerance, true);
    }

    public Reality atLeast(Reality other) {
        if (!this.possible) {
            return other;
        }
        if (!other.possible) {
            return this;
        }
        return this.bound >= other.bound ? this : other;
    }

    @Override
    public String toString() {
        return this.possible ? "Reality[" + this.bound + "]" : "Reality[impossible]";
    }
}

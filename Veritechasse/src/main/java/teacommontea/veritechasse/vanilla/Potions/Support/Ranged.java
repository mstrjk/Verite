package teacommontea.veritechasse.vanilla.Potions.Support;

public final class Ranged {

    private final double base;
    private final double min;
    private final double max;

    public Ranged(double base, double min, double max) {
        this.base = base;
        this.min = min;
        this.max = max;
    }

    public double base() {
        return this.base;
    }

    public double min() {
        return this.min;
    }

    public double max() {
        return this.max;
    }

    public double sanitize(double value) {
        if (Double.isNaN(value)) {
            return this.min;
        }
        if (value < this.min) {
            return this.min;
        }
        if (value > this.max) {
            return this.max;
        }
        return value;
    }
}

package teacommontea.veritechasse.Vanilla.Enchantments.Maths;

public final class ClampedValue implements LevelValue {

    private final LevelValue value;
    private final float min;
    private final float max;

    public ClampedValue(LevelValue value, float min, float max) {
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public LevelValue value() {
        return this.value;
    }

    public float min() {
        return this.min;
    }

    public float max() {
        return this.max;
    }

    @Override
    public float calculate(int level) {
        float raw = this.value.calculate(level);
        if (raw < this.min) {
            return this.min;
        }
        if (raw > this.max) {
            return this.max;
        }
        return raw;
    }
}

package teacommontea.veritechasse.Vanilla.Enchantments.Maths;

public final class LookupValue implements LevelValue {

    private final float[] values;
    private final LevelValue fallback;

    public LookupValue(float[] values, LevelValue fallback) {
        this.values = values.clone();
        this.fallback = fallback;
    }

    public int size() {
        return this.values.length;
    }

    public LevelValue fallback() {
        return this.fallback;
    }

    @Override
    public float calculate(int level) {
        if (level >= 1 && level <= this.values.length) {
            return this.values[level - 1];
        }
        return this.fallback.calculate(level);
    }
}

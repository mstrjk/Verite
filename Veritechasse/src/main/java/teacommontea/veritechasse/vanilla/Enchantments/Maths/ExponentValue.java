package teacommontea.veritechasse.vanilla.Enchantments.Maths;

public final class ExponentValue implements LevelValue {

    private final LevelValue base;
    private final LevelValue power;

    public ExponentValue(LevelValue base, LevelValue power) {
        this.base = base;
        this.power = power;
    }

    public LevelValue base() {
        return this.base;
    }

    public LevelValue power() {
        return this.power;
    }

    @Override
    public float calculate(int level) {
        return (float) Math.pow(this.base.calculate(level), this.power.calculate(level));
    }
}

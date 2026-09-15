package teacommontea.veritechasse.vanilla.Enchantments.Maths;

public final class FractionValue implements LevelValue {

    private final LevelValue numerator;
    private final LevelValue denominator;

    public FractionValue(LevelValue numerator, LevelValue denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public LevelValue numerator() {
        return this.numerator;
    }

    public LevelValue denominator() {
        return this.denominator;
    }

    @Override
    public float calculate(int level) {
        float divisor = this.denominator.calculate(level);
        if (divisor == 0.0F) {
            return 0.0F;
        }
        return this.numerator.calculate(level) / divisor;
    }
}

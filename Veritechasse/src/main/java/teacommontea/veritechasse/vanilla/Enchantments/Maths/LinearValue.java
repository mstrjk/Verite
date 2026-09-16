package teacommontea.veritechasse.Vanilla.Enchantments.Maths;

public final class LinearValue implements LevelValue {

    private final float base;
    private final float perLevelAboveFirst;

    public LinearValue(float base, float perLevelAboveFirst) {
        this.base = base;
        this.perLevelAboveFirst = perLevelAboveFirst;
    }

    public float base() {
        return this.base;
    }

    public float perLevelAboveFirst() {
        return this.perLevelAboveFirst;
    }

    @Override
    public float calculate(int level) {
        return this.base + this.perLevelAboveFirst * (float) (level - 1);
    }
}

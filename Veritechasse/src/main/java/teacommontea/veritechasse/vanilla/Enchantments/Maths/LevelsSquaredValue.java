package teacommontea.veritechasse.Vanilla.Enchantments.Maths;

public final class LevelsSquaredValue implements LevelValue {

    private final float added;

    public LevelsSquaredValue(float added) {
        this.added = added;
    }

    public float added() {
        return this.added;
    }

    @Override
    public float calculate(int level) {
        return (float) (level * level) + this.added;
    }
}

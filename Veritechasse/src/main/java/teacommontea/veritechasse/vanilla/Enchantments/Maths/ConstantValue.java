package teacommontea.veritechasse.Vanilla.Enchantments.Maths;

public final class ConstantValue implements LevelValue {

    private final float value;

    public ConstantValue(float value) {
        this.value = value;
    }

    public float value() {
        return this.value;
    }

    @Override
    public float calculate(int level) {
        return this.value;
    }
}

package teacommontea.veritechasse.vanilla.Enchantments.Support;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;

public final class Cost {

    private final LevelValue min;
    private final LevelValue max;
    private final int anvil;

    public Cost(LevelValue min, LevelValue max, int anvil) {
        this.min = min;
        this.max = max;
        this.anvil = anvil;
    }

    public int min(int level) {
        return (int) this.min.calculate(level);
    }

    public int max(int level) {
        return (int) this.max.calculate(level);
    }

    public int anvil() {
        return this.anvil;
    }

    public boolean permits(int level, int enchantingCost) {
        return enchantingCost >= min(level) && enchantingCost <= max(level);
    }

    public int anvilCostFor(int level) {
        return this.anvil * level;
    }
}

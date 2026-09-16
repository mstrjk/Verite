package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableLegArmor;
import teacommontea.veritechasse.Vanilla.Era;

public final class SwiftSneak {

    public static final String KEY = "swift_sneak";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableLegArmor.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(25.0F, 25.0F),
        new LinearValue(75.0F, 25.0F),
        8);

    public static final float BASE_SNEAKING_SPEED = 0.3F;
    public static final float ATTRIBUTE_MAX = 1.0F;

    private static final LevelValue SNEAKING_SPEED = new LinearValue(0.15F, 0.15F);

    private SwiftSneak() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float sneakingSpeedBonus(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return SNEAKING_SPEED.calculate(level);
    }

    public static float sneakingSpeed(int level, Era era) {
        float speed = BASE_SNEAKING_SPEED + sneakingSpeedBonus(level, era);
        return speed > ATTRIBUTE_MAX ? ATTRIBUTE_MAX : speed;
    }
}

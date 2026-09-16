package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableFireAspect;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableMeleeWeapon;
import teacommontea.veritechasse.Vanilla.Era;

public final class FireAspect {

    public static final String KEY = "fire_aspect";
    public static final int VANILLA_MAX_LEVEL = 2;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableFireAspect.KEY;
    public static final String PRIMARY_ITEMS = EnchantableMeleeWeapon.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(10.0F, 20.0F),
        new LinearValue(60.0F, 20.0F),
        4);

    private static final LevelValue IGNITE_SECONDS = new LinearValue(4.0F, 4.0F);

    private FireAspect() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float igniteSeconds(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return IGNITE_SECONDS.calculate(level);
    }

    public static int igniteTicks(int level, Era era) {
        return (int) (igniteSeconds(level, era) * 20.0F);
    }

    public static boolean requiresDirectDamage() {
        return true;
    }
}

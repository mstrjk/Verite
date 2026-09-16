package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableWeapon;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetDamage;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.SensitiveToSmite;
import teacommontea.veritechasse.Vanilla.Era;

public final class Smite {

    public static final String KEY = "smite";
    public static final int VANILLA_MAX_LEVEL = 5;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableWeapon.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetDamage.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 8.0F),
        new LinearValue(25.0F, 8.0F),
        2);

    private static final LevelValue DAMAGE = new LinearValue(2.5F, 2.5F);

    public static final float LEGACY_DAMAGE_PER_LEVEL = 2.5F;

    private Smite() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float bonusDamage(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return DAMAGE.calculate(level);
        }
        return (float) level * LEGACY_DAMAGE_PER_LEVEL;
    }

    public static float bonusDamageAgainst(int level, EntityType target, Era era) {
        return SensitiveToSmite.TAG.contains(target) ? bonusDamage(level, era) : 0.0F;
    }
}

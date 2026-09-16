package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableWeapon;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetDamage;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.SensitiveToBaneOfArthropods;
import teacommontea.veritechasse.Vanilla.Era;

public final class BaneOfArthropods {

    public static final String KEY = "bane_of_arthropods";
    public static final int VANILLA_MAX_LEVEL = 5;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableWeapon.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetDamage.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 8.0F),
        new LinearValue(25.0F, 8.0F),
        2);

    public static final int SLOWNESS_AMPLIFIER = 3;
    public static final float SLOWNESS_MIN_SECONDS = 1.5F;

    public static final float LEGACY_DAMAGE_PER_LEVEL = 2.5F;
    public static final int LEGACY_SLOWNESS_BASE_TICKS = 20;
    public static final int LEGACY_SLOWNESS_RANDOM_PER_LEVEL = 10;

    private static final LevelValue DAMAGE = new LinearValue(2.5F, 2.5F);
    private static final LevelValue SLOWNESS_MAX_SECONDS = new LinearValue(1.5F, 0.5F);

    private BaneOfArthropods() {
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
        return SensitiveToBaneOfArthropods.TAG.contains(target) ? bonusDamage(level, era) : 0.0F;
    }

    public static float slownessMaxSeconds(int level) {
        if (level <= 0) {
            return 0.0F;
        }
        return SLOWNESS_MAX_SECONDS.calculate(level);
    }

    public static int slownessMaxTicks(int level) {
        return (int) (slownessMaxSeconds(level) * 20.0F);
    }

    public static int slownessMinTicks(int level, Era era) {
        if (level <= 0) {
            return 0;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return (int) (SLOWNESS_MIN_SECONDS * 20.0F);
        }
        return LEGACY_SLOWNESS_BASE_TICKS;
    }

    public static int slownessMaxTicks(int level, Era era) {
        if (level <= 0) {
            return 0;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return slownessMaxTicks(level);
        }
        return LEGACY_SLOWNESS_BASE_TICKS + LEGACY_SLOWNESS_RANDOM_PER_LEVEL * level - 1;
    }

    public static boolean slownessDurationIsPossible(int observedTicks, int level, Era era) {
        if (level <= 0) {
            return observedTicks == 0;
        }
        return observedTicks >= slownessMinTicks(level, era)
            && observedTicks <= slownessMaxTicks(level, era);
    }
}

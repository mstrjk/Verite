package teacommontea.veritechasse.vanilla.Enchantments.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.Infinity;
import teacommontea.veritechasse.vanilla.Enchantments.Multishot;
import teacommontea.veritechasse.vanilla.Enchantments.Piercing;
import teacommontea.veritechasse.vanilla.Enchantments.QuickCharge;

public final class RangedModifiers {

    public static final int DEFAULT_PROJECTILE_COUNT = 1;
    public static final float DEFAULT_SPREAD = 0.0F;

    private RangedModifiers() {
    }

    public static int projectileCount(ItemStack weapon, Era era) {
        if (weapon == null) {
            return DEFAULT_PROJECTILE_COUNT;
        }
        int level = Multishot.levelOn(weapon);
        if (level <= 0) {
            return DEFAULT_PROJECTILE_COUNT;
        }
        return Multishot.projectileCount(level, era);
    }

    public static float projectileSpread(ItemStack weapon, Era era) {
        if (weapon == null) {
            return DEFAULT_SPREAD;
        }
        int level = Multishot.levelOn(weapon);
        if (level <= 0) {
            return DEFAULT_SPREAD;
        }
        return Multishot.projectileSpreadDegrees(level, era);
    }

    public static int piercingCount(ItemStack weapon, Era era) {
        if (weapon == null) {
            return 0;
        }
        return Piercing.piercingCount(Piercing.levelOn(weapon), era);
    }

    public static int entitiesPassedThrough(ItemStack weapon, Era era) {
        if (weapon == null) {
            return 1;
        }
        return Piercing.entitiesPassedThrough(Piercing.levelOn(weapon), era);
    }

    public static float chargeTimeSeconds(ItemStack weapon, float baseSeconds, Era era) {
        if (weapon == null) {
            return baseSeconds;
        }
        return baseSeconds + QuickCharge.chargeTimeModifier(QuickCharge.levelOn(weapon), era);
    }

    public static int chargeDurationTicks(ItemStack weapon, float baseSeconds, Era era) {
        float seconds = chargeTimeSeconds(weapon, baseSeconds, era);
        if (seconds < 0.0F) {
            return 0;
        }
        return (int) Math.floor((double) (seconds * 20.0F));
    }

    public static boolean consumesAmmo(ItemStack weapon, String ammoItem) {
        if (weapon == null) {
            return true;
        }
        return Infinity.consumesAmmo(Infinity.levelOn(weapon), ammoItem);
    }

    public static int ammoUse(ItemStack weapon, String ammoItem) {
        if (weapon == null) {
            return 1;
        }
        return Infinity.ammoUse(Infinity.levelOn(weapon), ammoItem);
    }
}

package teacommontea.veritechasse.vanilla.Enchantments.Support;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.BaneOfArthropods;
import teacommontea.veritechasse.vanilla.Enchantments.Channeling;
import teacommontea.veritechasse.vanilla.Enchantments.FireAspect;
import teacommontea.veritechasse.vanilla.Enchantments.Flame;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.SensitiveToBaneOfArthropods;
import teacommontea.veritechasse.vanilla.Enchantments.WindBurst;

public final class PostAttackEffects {

    private PostAttackEffects() {
    }

    public static int igniteTicks(ItemStack stack, Era era) {
        if (stack == null) {
            return 0;
        }
        return FireAspect.igniteTicks(FireAspect.levelOn(stack), era);
    }

    public static boolean ignites(ItemStack stack, boolean directDamage, Era era) {
        if (igniteTicks(stack, era) <= 0) {
            return false;
        }
        return !FireAspect.requiresDirectDamage() || directDamage;
    }

    public static int projectileIgniteTicks(ItemStack stack) {
        if (stack == null) {
            return 0;
        }
        return Flame.igniteTicks(Flame.levelOn(stack));
    }

    public static int slownessTicks(ItemStack stack, EntityType target, Era era) {
        if (stack == null) {
            return 0;
        }
        if (!SensitiveToBaneOfArthropods.TAG.contains(target)) {
            return 0;
        }
        return BaneOfArthropods.slownessMaxTicks(BaneOfArthropods.levelOn(stack));
    }

    public static float windBurstKnockback(ItemStack stack, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        return WindBurst.knockbackMultiplier(WindBurst.levelOn(stack), era);
    }

    public static boolean channelingStrikesOnEntityHit(ItemStack stack, boolean thundering, boolean victimCanSeeSky) {
        if (stack == null) {
            return false;
        }
        return Channeling.strikesOnEntityHit(Channeling.levelOn(stack), thundering, victimCanSeeSky);
    }
}

package teacommontea.veritechasse.Vanilla.Enchantments.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.AquaAffinity;
import teacommontea.veritechasse.Vanilla.Enchantments.Efficiency;
import teacommontea.veritechasse.Vanilla.Enchantments.SilkTouch;

public final class MiningModifiers {

    public static final float DEFAULT_SUBMERGED_MINING_SPEED = 0.2F;

    private MiningModifiers() {
    }

    public static int efficiencyLevel(ItemStack stack) {
        return stack == null ? 0 : Efficiency.levelOn(stack);
    }

    public static float miningEfficiency(ItemStack stack, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        return Efficiency.miningEfficiency(Efficiency.levelOn(stack), era);
    }

    public static boolean hasAquaAffinity(ItemStack helmet) {
        return helmet != null && AquaAffinity.levelOn(helmet) > 0;
    }

    public static float submergedMiningSpeed(ItemStack helmet, Era era) {
        if (helmet == null) {
            return DEFAULT_SUBMERGED_MINING_SPEED;
        }
        return AquaAffinity.submergedMiningSpeed(AquaAffinity.levelOn(helmet), era);
    }

    public static boolean hasSilkTouch(ItemStack stack) {
        return stack != null && SilkTouch.levelOn(stack) > 0;
    }

    public static boolean dropsExperience(ItemStack stack) {
        if (stack == null) {
            return true;
        }
        return SilkTouch.dropsExperience(SilkTouch.levelOn(stack));
    }
}

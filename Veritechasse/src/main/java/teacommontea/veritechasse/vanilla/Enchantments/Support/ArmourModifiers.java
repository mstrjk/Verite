package teacommontea.veritechasse.vanilla.Enchantments.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.BindingCurse;
import teacommontea.veritechasse.vanilla.Enchantments.DepthStrider;
import teacommontea.veritechasse.vanilla.Enchantments.FrostWalker;
import teacommontea.veritechasse.vanilla.Enchantments.Respiration;
import teacommontea.veritechasse.vanilla.Enchantments.SoulSpeed;
import teacommontea.veritechasse.vanilla.Enchantments.SwiftSneak;
import teacommontea.veritechasse.vanilla.Enchantments.Thorns;
import teacommontea.veritechasse.vanilla.Era;

public final class ArmourModifiers {

    private ArmourModifiers() {
    }

    public static float thornsTriggerChance(ItemStack stack, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        return Thorns.triggerChance(Thorns.levelOn(stack), era);
    }

    public static float thornsMaxReflectedDamage(ItemStack stack, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        return Thorns.maxReflectedDamage(Thorns.levelOn(stack), era);
    }

    public static float thornsMaxReflectedDamage(
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            Era era) {
        float highest = 0.0F;
        highest = higher(highest, thornsMaxReflectedDamage(helmet, era));
        highest = higher(highest, thornsMaxReflectedDamage(chestplate, era));
        highest = higher(highest, thornsMaxReflectedDamage(leggings, era));
        return higher(highest, thornsMaxReflectedDamage(boots, era));
    }

    public static float anyThornsTriggerChance(
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            Era era) {
        float highest = 0.0F;
        highest = higher(highest, thornsTriggerChance(helmet, era));
        highest = higher(highest, thornsTriggerChance(chestplate, era));
        highest = higher(highest, thornsTriggerChance(leggings, era));
        return higher(highest, thornsTriggerChance(boots, era));
    }

    public static float soulSpeedBonus(ItemStack boots, Era era) {
        if (boots == null) {
            return 0.0F;
        }
        return SoulSpeed.movementSpeedBonus(SoulSpeed.levelOn(boots), era);
    }

    public static boolean soulSpeedApplies(
            ItemStack boots,
            boolean onSoulBlock,
            boolean onGround,
            boolean flying,
            boolean riding) {
        if (boots == null) {
            return false;
        }
        return SoulSpeed.applies(SoulSpeed.levelOn(boots), onSoulBlock, onGround, flying, riding);
    }

    public static float sneakingSpeed(ItemStack leggings, Era era) {
        if (leggings == null) {
            return SwiftSneak.BASE_SNEAKING_SPEED;
        }
        return SwiftSneak.sneakingSpeed(SwiftSneak.levelOn(leggings), era);
    }

    public static float sneakingSpeedBonus(ItemStack leggings, Era era) {
        if (leggings == null) {
            return 0.0F;
        }
        return SwiftSneak.sneakingSpeedBonus(SwiftSneak.levelOn(leggings), era);
    }

    public static boolean frostWalkerFreezes(ItemStack boots, boolean onGround, boolean riding) {
        if (boots == null) {
            return false;
        }
        return FrostWalker.freezes(FrostWalker.levelOn(boots), onGround, riding);
    }

    public static float frostWalkerRadius(ItemStack boots, Era era) {
        if (boots == null) {
            return 0.0F;
        }
        return FrostWalker.freezeRadius(FrostWalker.levelOn(boots), era);
    }

    public static boolean immuneToSteppingBurn(ItemStack boots, String damageType) {
        if (boots == null) {
            return false;
        }
        return FrostWalker.immuneToSteppingBurn(FrostWalker.levelOn(boots), damageType);
    }

    public static float respirationOxygenBonus(ItemStack helmet, Era era) {
        if (helmet == null) {
            return 0.0F;
        }
        return Respiration.oxygenBonus(Respiration.levelOn(helmet), era);
    }

    public static float respirationAirLossChance(ItemStack helmet, Era era) {
        if (helmet == null) {
            return 1.0F;
        }
        return Respiration.airLossChancePerTick(Respiration.levelOn(helmet), era);
    }

    public static float expectedTicksUnderwater(ItemStack helmet, Era era) {
        if (helmet == null) {
            return Respiration.BASE_AIR_TICKS;
        }
        return Respiration.expectedTicksUnderwater(Respiration.levelOn(helmet), era);
    }

    public static float waterMovementEfficiency(ItemStack boots, Era era) {
        if (boots == null) {
            return 0.0F;
        }
        return DepthStrider.waterMovementEfficiency(DepthStrider.levelOn(boots), era);
    }

    public static float waterSlowDown(float baseSlowDown, ItemStack boots, boolean onGround, Era era) {
        if (boots == null) {
            return baseSlowDown;
        }
        return DepthStrider.waterSlowDown(baseSlowDown, DepthStrider.levelOn(boots), onGround, era);
    }

    public static boolean preventsArmourChange(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return BindingCurse.preventsArmorChange(BindingCurse.levelOn(stack));
    }

    public static boolean canBeRemoved(ItemStack stack, boolean creative, boolean itemBroke, boolean died) {
        if (stack == null) {
            return true;
        }
        return BindingCurse.canBeRemoved(BindingCurse.levelOn(stack), creative, itemBroke, died);
    }

    private static float higher(float current, float candidate) {
        return candidate > current ? candidate : current;
    }
}

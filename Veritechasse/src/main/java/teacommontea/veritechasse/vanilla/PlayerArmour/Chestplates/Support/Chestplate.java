package teacommontea.veritechasse.vanilla.PlayerArmour.Chestplates.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Support.ProtectionModifiers;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.ArmourType;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.Reality;

public final class Chestplate {

    public static final ArmourType TYPE = ArmourType.CHESTPLATE;

    public static final int UNIT_DURABILITY = 16;

    private Chestplate() {
    }

    public static int defence(Material material) {
        return material.defenceChestplate();
    }

    public static float toughness(Material material) {
        return material.toughness();
    }

    public static float knockbackResistance(Material material) {
        return material.knockbackResistance();
    }

    public static int maxDamage(Material material) {
        return TYPE.durability(material.durabilityMultiplier());
    }

    public static int enchantmentValue(Material material) {
        return material.enchantmentValue();
    }

    public static float protection(
            ItemStack stack,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        return ProtectionModifiers.protectionOn(stack, isFire, isExplosion, isProjectile, isFall, era);
    }

    public static Reality defenceFrom(Material material) {
        return Reality.of(defence(material));
    }

    public static Reality toughnessFrom(Material material) {
        return Reality.of(toughness(material));
    }
}

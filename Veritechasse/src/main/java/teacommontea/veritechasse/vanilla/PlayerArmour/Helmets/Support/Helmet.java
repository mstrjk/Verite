package teacommontea.veritechasse.Vanilla.PlayerArmour.Helmets.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Support.ProtectionModifiers;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.ArmourType;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Helmet {

    public static final ArmourType TYPE = ArmourType.HELMET;

    public static final int UNIT_DURABILITY = 11;

    private Helmet() {
    }

    public static int defence(Material material) {
        return material.defenceHelmet();
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

package teacommontea.veritechasse.vanilla.PlayerArmour.Leggings;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Leggings.Support.Leggings;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.Reality;

public final class CopperLeggings {

    public static final String KEY = "copper_leggings";
    public static final Material MATERIAL = Materials.COPPER;

    public static final int MAX_DAMAGE = 165;
    public static final int DEFENCE = 3;
    public static final int ENCHANTMENT_VALUE = 8;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private CopperLeggings() {
    }

    public static int defence() {
        return Leggings.defence(MATERIAL);
    }

    public static float toughness() {
        return Leggings.toughness(MATERIAL);
    }

    public static float knockbackResistance() {
        return Leggings.knockbackResistance(MATERIAL);
    }

    public static float protection(
            ItemStack stack,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        return Leggings.protection(stack, isFire, isExplosion, isProjectile, isFall, era);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

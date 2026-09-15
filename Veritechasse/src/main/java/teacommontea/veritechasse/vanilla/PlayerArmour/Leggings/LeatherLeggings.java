package teacommontea.veritechasse.vanilla.PlayerArmour.Leggings;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Leggings.Support.Leggings;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.Reality;

public final class LeatherLeggings {

    public static final String KEY = "leather_leggings";
    public static final Material MATERIAL = Materials.LEATHER;

    public static final int MAX_DAMAGE = 75;
    public static final int DEFENCE = 2;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private LeatherLeggings() {
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

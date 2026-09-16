package teacommontea.veritechasse.Vanilla.PlayerArmour.Leggings;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Leggings.Support.Leggings;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.Vanilla.Reality;

public final class GoldenLeggings {

    public static final String KEY = "golden_leggings";
    public static final Material MATERIAL = Materials.GOLD;

    public static final int MAX_DAMAGE = 105;
    public static final int DEFENCE = 3;
    public static final int ENCHANTMENT_VALUE = 25;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private GoldenLeggings() {
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

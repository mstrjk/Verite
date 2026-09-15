package teacommontea.veritechasse.vanilla.PlayerArmour.Chestplates;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Chestplates.Support.Chestplate;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.Reality;

public final class GoldenChestplate {

    public static final String KEY = "golden_chestplate";
    public static final Material MATERIAL = Materials.GOLD;

    public static final int MAX_DAMAGE = 112;
    public static final int DEFENCE = 5;
    public static final int ENCHANTMENT_VALUE = 25;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private GoldenChestplate() {
    }

    public static int defence() {
        return Chestplate.defence(MATERIAL);
    }

    public static float toughness() {
        return Chestplate.toughness(MATERIAL);
    }

    public static float knockbackResistance() {
        return Chestplate.knockbackResistance(MATERIAL);
    }

    public static float protection(
            ItemStack stack,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        return Chestplate.protection(stack, isFire, isExplosion, isProjectile, isFall, era);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

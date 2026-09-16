package teacommontea.veritechasse.Vanilla.PlayerArmour.Chestplates;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Chestplates.Support.Chestplate;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.Vanilla.Reality;

public final class CopperChestplate {

    public static final String KEY = "copper_chestplate";
    public static final Material MATERIAL = Materials.COPPER;

    public static final int MAX_DAMAGE = 176;
    public static final int DEFENCE = 4;
    public static final int ENCHANTMENT_VALUE = 8;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private CopperChestplate() {
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

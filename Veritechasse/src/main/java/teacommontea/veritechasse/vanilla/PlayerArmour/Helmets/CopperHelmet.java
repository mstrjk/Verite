package teacommontea.veritechasse.Vanilla.PlayerArmour.Helmets;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Helmets.Support.Helmet;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.Vanilla.Reality;

public final class CopperHelmet {

    public static final String KEY = "copper_helmet";
    public static final Material MATERIAL = Materials.COPPER;

    public static final int MAX_DAMAGE = 121;
    public static final int DEFENCE = 2;
    public static final int ENCHANTMENT_VALUE = 8;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private CopperHelmet() {
    }

    public static int defence() {
        return Helmet.defence(MATERIAL);
    }

    public static float toughness() {
        return Helmet.toughness(MATERIAL);
    }

    public static float knockbackResistance() {
        return Helmet.knockbackResistance(MATERIAL);
    }

    public static float protection(
            ItemStack stack,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        return Helmet.protection(stack, isFire, isExplosion, isProjectile, isFall, era);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

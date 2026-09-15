package teacommontea.veritechasse.vanilla.PlayerArmour.Helmets;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Helmets.Support.Helmet;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.Reality;

public final class DiamondHelmet {

    public static final String KEY = "diamond_helmet";
    public static final Material MATERIAL = Materials.DIAMOND;

    public static final int MAX_DAMAGE = 363;
    public static final int DEFENCE = 3;
    public static final int ENCHANTMENT_VALUE = 10;
    public static final float TOUGHNESS = 2.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private DiamondHelmet() {
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

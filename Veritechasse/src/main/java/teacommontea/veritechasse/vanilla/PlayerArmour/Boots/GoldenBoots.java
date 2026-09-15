package teacommontea.veritechasse.vanilla.PlayerArmour.Boots;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Boots.Support.Boots;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.Reality;

public final class GoldenBoots {

    public static final String KEY = "golden_boots";
    public static final Material MATERIAL = Materials.GOLD;

    public static final int MAX_DAMAGE = 91;
    public static final int DEFENCE = 1;
    public static final int ENCHANTMENT_VALUE = 25;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    private GoldenBoots() {
    }

    public static int defence() {
        return Boots.defence(MATERIAL);
    }

    public static float toughness() {
        return Boots.toughness(MATERIAL);
    }

    public static float knockbackResistance() {
        return Boots.knockbackResistance(MATERIAL);
    }

    public static float protection(
            ItemStack stack,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        return Boots.protection(stack, isFire, isExplosion, isProjectile, isFall, era);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

package teacommontea.veritechasse.vanilla.PlayerArmour.Leggings;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Leggings.Support.Leggings;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.Reality;

public final class NetheriteLeggings {

    public static final String KEY = "netherite_leggings";
    public static final Material MATERIAL = Materials.NETHERITE;

    public static final int MAX_DAMAGE = 555;
    public static final int DEFENCE = 6;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final float TOUGHNESS = 3.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.1F;

    public static final boolean FIRE_RESISTANT = true;

    private NetheriteLeggings() {
    }

    public static boolean survivesLava() {
        return FIRE_RESISTANT;
    }

    public static boolean survivesFire() {
        return FIRE_RESISTANT;
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

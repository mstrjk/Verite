package teacommontea.veritechasse.Vanilla.PlayerArmour.Boots;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Boots.Support.Boots;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.Vanilla.Reality;

public final class NetheriteBoots {

    public static final String KEY = "netherite_boots";
    public static final Material MATERIAL = Materials.NETHERITE;

    public static final int MAX_DAMAGE = 481;
    public static final int DEFENCE = 3;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final float TOUGHNESS = 3.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.1F;

    public static final boolean FIRE_RESISTANT = true;

    private NetheriteBoots() {
    }

    public static boolean survivesLava() {
        return FIRE_RESISTANT;
    }

    public static boolean survivesFire() {
        return FIRE_RESISTANT;
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

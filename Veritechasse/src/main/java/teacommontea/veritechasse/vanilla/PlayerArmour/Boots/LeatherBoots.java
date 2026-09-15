package teacommontea.veritechasse.vanilla.PlayerArmour.Boots;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Boots.Support.Boots;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.PlayerArmour.Tags.PowderSnowWalkableMobs;
import teacommontea.veritechasse.vanilla.Reality;

public final class LeatherBoots {

    public static final String KEY = "leather_boots";
    public static final Material MATERIAL = Materials.LEATHER;

    public static final int MAX_DAMAGE = 65;
    public static final int DEFENCE = 1;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    public static final boolean WALKS_ON_POWDER_SNOW = true;

    private LeatherBoots() {
    }

    public static boolean walksOnPowderSnow(ItemStack feet) {
        return feet != null && feet.getType().name().toLowerCase(Locale.ROOT).equals(KEY);
    }

    public static boolean entityWalksOnPowderSnow(String entityName, ItemStack feet) {
        if (PowderSnowWalkableMobs.contains(entityName)) {
            return true;
        }
        return walksOnPowderSnow(feet);
    }

    public static boolean sinksInPowderSnow(String entityName, ItemStack feet) {
        return !entityWalksOnPowderSnow(entityName, feet);
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

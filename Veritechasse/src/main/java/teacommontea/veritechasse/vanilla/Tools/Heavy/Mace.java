package teacommontea.veritechasse.Vanilla.Tools.Heavy;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.Density;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Heavy.Support.SmashAttack;
import teacommontea.veritechasse.Vanilla.Tools.Support.PlayerBase;

public final class Mace {

    public static final String KEY = "mace";

    public static final int MAX_DAMAGE = 500;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final int DAMAGE_PER_ATTACK = 1;
    public static final int DAMAGE_PER_BLOCK = 2;

    public static final String REPAIRS_WITH = "breeze_rod";

    public static final double ATTACK_DAMAGE_BASELINE = 5.0D;
    public static final double ATTACK_SPEED_BASELINE = -3.4D;

    public static final String RARITY = "epic";
    public static final String DAMAGE_TYPE = "mace_smash";

    public static final boolean ENCHANTABLE = true;
    public static final boolean FIRE_RESISTANT = false;
    public static final boolean CAN_DESTROY_BLOCKS_IN_CREATIVE = false;

    public static final double DENSITY_PER_LEVEL = 0.5D;

    private Mace() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static double attackDamage() {
        return PlayerBase.ATTACK_DAMAGE + ATTACK_DAMAGE_BASELINE;
    }

    public static double attackSpeed() {
        return PlayerBase.ATTACK_SPEED + ATTACK_SPEED_BASELINE;
    }

    public static double attackCooldownTicks() {
        return 20.0D / attackSpeed();
    }

    public static boolean canSmash(double fallDistance, boolean fallFlying) {
        return SmashAttack.canSmash(fallDistance, fallFlying);
    }

    public static double smashBonus(double fallDistance, boolean fallFlying) {
        return SmashAttack.bonusDamage(fallDistance, fallFlying);
    }

    public static double densityBonusPerBlock(int densityLevel) {
        if (densityLevel <= 0) {
            return 0.0D;
        }
        return DENSITY_PER_LEVEL * (double) densityLevel;
    }

    public static double totalDamage(double fallDistance, boolean fallFlying, int densityLevel) {
        double base = attackDamage();
        if (!canSmash(fallDistance, fallFlying)) {
            return base;
        }
        double smash = SmashAttack.bonusDamage(fallDistance);
        double density = densityBonusPerBlock(densityLevel) * fallDistance;
        return base + smash + density;
    }

    public static double knockbackPower(double distanceToTarget, double fallDistance, double knockbackResistance) {
        return SmashAttack.knockbackPower(distanceToTarget, fallDistance, knockbackResistance);
    }

    public static boolean negatesOwnFallDamage(double fallDistance, boolean fallFlying) {
        return canSmash(fallDistance, fallFlying);
    }

    public static double resolvedTotalDamage(ItemStack mace, double fallDistance, boolean fallFlying, Era era) {
        int densityLevel = mace == null ? 0 : Density.levelOn(mace);
        return totalDamage(fallDistance, fallFlying, densityLevel);
    }

    public static Reality damageFrom(double fallDistance, boolean fallFlying, int densityLevel) {
        return Reality.of(totalDamage(fallDistance, fallFlying, densityLevel));
    }

    public static Reality resolvedDamageFrom(ItemStack mace, double fallDistance, boolean fallFlying, Era era) {
        return Reality.of(resolvedTotalDamage(mace, fallDistance, fallFlying, era));
    }

    public static int remaining(int damage) {
        int left = MAX_DAMAGE - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(int damage) {
        return damage >= MAX_DAMAGE;
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(remaining(damage));
    }
}

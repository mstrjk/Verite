package teacommontea.veritechasse.vanilla.PlayerArmour.Shields;

import java.util.Locale;
import java.util.Set;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.BindingCurse;
import teacommontea.veritechasse.vanilla.Enchantments.Mending;
import teacommontea.veritechasse.vanilla.Enchantments.Support.DurabilityModifiers;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Exclusivity;
import teacommontea.veritechasse.vanilla.Enchantments.Support.RangedModifiers;
import teacommontea.veritechasse.vanilla.Enchantments.Unbreaking;
import teacommontea.veritechasse.vanilla.Enchantments.VanishingCurse;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Shields.Support.BlocksAttacks;
import teacommontea.veritechasse.vanilla.PlayerArmour.Shields.Support.ShieldDisable;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class Shield {

    public static final String KEY = "shield";

    public static final int MAX_DAMAGE = 336;
    public static final int USE_DURATION_TICKS = 72000;

    public static final String SLOT_NAME = "offhand";
    public static final boolean UNSWAPPABLE = true;
    public static final boolean FIRE_RESISTANT = false;

    public static final int DEFENCE = 0;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    private static final Set<String> LEGAL_ENCHANTS = Set.of(
        Unbreaking.KEY,
        Mending.KEY,
        VanishingCurse.KEY,
        BindingCurse.KEY);

    public static final boolean USE_PERMITS_SPRINTING = false;
    public static final float USE_SPEED_MULTIPLIER = 0.2F;

    public static boolean slowsWhileUsed() {
        return USE_SPEED_MULTIPLIER < 1.0F;
    }

    public static float useSpeedMultiplier() {
        return USE_SPEED_MULTIPLIER;
    }

    public static boolean permitsSprintingWhileUsed() {
        return USE_PERMITS_SPRINTING;
    }

    private Shield() {
    }

    public static boolean is(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getType().name().toLowerCase(Locale.ROOT).equals(KEY);
    }

    public static Set<String> legalEnchants() {
        return LEGAL_ENCHANTS;
    }

    public static boolean canHold(Enchantment enchantment) {
        return enchantment != null && LEGAL_ENCHANTS.contains(Exclusivity.keyOf(enchantment));
    }

    public static boolean enchantmentsAreLegal(ItemStack stack) {
        if (!is(stack)) {
            return false;
        }
        for (Enchantment enchantment : stack.getEnchantments().keySet()) {
            if (!canHold(enchantment)) {
                return false;
            }
        }
        return Exclusivity.combinationIsLegal(stack);
    }

    public static boolean blockingIsEffective(int ticksHeld) {
        return BlocksAttacks.delayElapsed(ticksHeld);
    }

    public static boolean withinBlockingAngle(double angleRadians) {
        return BlocksAttacks.withinBlockingAngle(angleRadians);
    }

    public static float damageAfterBlocking(float dealtDamage, double angleRadians, int ticksHeld, boolean bypassed) {
        if (!blockingIsEffective(ticksHeld)) {
            return dealtDamage;
        }
        return BlocksAttacks.damageAfterBlocking(dealtDamage, angleRadians, bypassed);
    }

    public static boolean piercedBy(ItemStack attackerWeapon, Era era) {
        if (attackerWeapon == null) {
            return false;
        }
        return BlocksAttacks.piercingBypassesBlock(RangedModifiers.piercingCount(attackerWeapon, era));
    }

    public static float damageAfterBlocking(
            float dealtDamage,
            double angleRadians,
            int ticksHeld,
            boolean bypassed,
            ItemStack attackerWeapon,
            boolean projectile,
            Era era) {
        if (projectile && piercedBy(attackerWeapon, era)) {
            return dealtDamage;
        }
        return damageAfterBlocking(dealtDamage, angleRadians, ticksHeld, bypassed);
    }

    public static double horizontalAngleTo(
            double sourceX,
            double sourceZ,
            double victimX,
            double victimZ,
            double headYawRadians) {
        return BlocksAttacks.horizontalAngleTo(sourceX, sourceZ, victimX, victimZ, headYawRadians);
    }

    public static int durabilityCost(float dealtDamage) {
        return BlocksAttacks.itemDamageFrom(dealtDamage);
    }

    public static int disableCooldownTicks(Protocol protocol) {
        return ShieldDisable.cooldownTicks(protocol);
    }

    public static float disableChance(int efficiencyLevel, boolean sprinting, Protocol protocol) {
        return ShieldDisable.chance(efficiencyLevel, sprinting, protocol);
    }

    public static double expectedBlocks(ItemStack stack, Era era) {
        return DurabilityModifiers.expectedUses(stack, MAX_DAMAGE, era);
    }

    public static float durabilitySkipChance(ItemStack stack, Era era) {
        return DurabilityModifiers.skipChance(stack, true, era);
    }

    public static boolean repairIsPossible(ItemStack stack, int observedRepair, int experienceAbsorbed) {
        return DurabilityModifiers.repairIsPossible(stack, observedRepair, experienceAbsorbed);
    }

    public static Reality blockedDamageFrom(float dealtDamage, double angleRadians) {
        return BlocksAttacks.reductionFrom(dealtDamage, angleRadians);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }

    public static Reality useDurationFrom(int ticksHeld) {
        return Reality.of(USE_DURATION_TICKS - ticksHeld);
    }
}

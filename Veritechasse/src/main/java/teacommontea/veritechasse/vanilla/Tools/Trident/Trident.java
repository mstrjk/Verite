package teacommontea.veritechasse.vanilla.Tools.Trident;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Channeling;
import teacommontea.veritechasse.vanilla.Enchantments.Impaling;
import teacommontea.veritechasse.vanilla.Enchantments.Loyalty;
import teacommontea.veritechasse.vanilla.Enchantments.Mending;
import teacommontea.veritechasse.vanilla.Enchantments.Riptide;
import teacommontea.veritechasse.vanilla.Enchantments.Support.DurabilityModifiers;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Exclusivity;
import teacommontea.veritechasse.vanilla.Enchantments.Support.PostAttackEffects;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.SensitiveToImpaling;
import teacommontea.veritechasse.vanilla.Enchantments.Unbreaking;
import teacommontea.veritechasse.vanilla.Enchantments.VanishingCurse;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Support.PlayerBase;
import teacommontea.veritechasse.vanilla.Tools.Trident.Support.TridentDurability;
import teacommontea.veritechasse.vanilla.Tools.Trident.Support.TridentState;
import teacommontea.veritechasse.vanilla.Tools.Trident.Support.TridentUse;

public final class Trident {

    public static final String KEY = "trident";

    public static final int MAX_DAMAGE = 250;
    public static final int ENCHANTMENT_VALUE = 1;

    public static final int THROW_THRESHOLD_TIME = 10;
    public static final float BASE_DAMAGE = 8.0F;
    public static final float PROJECTILE_SHOOT_POWER = 2.5F;

    public static final double ATTACK_DAMAGE_BASELINE = 8.0D;
    public static final double ATTACK_SPEED_BASELINE = -2.9D;

    public static final boolean FIRE_RESISTANT = false;

    private static final Set<String> LEGAL_ENCHANTS = Set.of(
        Riptide.KEY,
        Loyalty.KEY,
        Channeling.KEY,
        Impaling.KEY,
        Unbreaking.KEY,
        Mending.KEY,
        VanishingCurse.KEY);

    private Trident() {
    }

    public static boolean is(ItemStack stack) {
        return stack != null && stack.getType() == Material.TRIDENT;
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

    public static boolean heldLongEnough(int ticksHeld) {
        return ticksHeld >= THROW_THRESHOLD_TIME;
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

    public static TridentState resolve(ItemStack stack, Protocol protocol) {
        return resolve(stack, null, protocol);
    }

    public static TridentState resolve(ItemStack stack, EntityType target, Protocol protocol) {
        TridentState state = new TridentState(PROJECTILE_SHOOT_POWER);
        if (!is(stack)) {
            return state;
        }

        Era era = Era.enchantments(protocol);

        int riptide = Riptide.levelOn(stack);
        if (riptide > 0) {
            state.addSpinAttackStrength(Riptide.spinAttackStrength(riptide, era));
            state.addThrowPower(Riptide.additionalThrowPower(riptide, era));
            state.spinAttackTicks(Riptide.SPIN_ATTACK_TICKS);
        }

        int loyalty = Loyalty.levelOn(stack);
        if (loyalty > 0) {
            state.addReturnAcceleration(Loyalty.returnSpeed(loyalty, era));
        }

        state.addBonusDamage(impalingBonus(stack, target, era));
        state.summonsLightning(Channeling.levelOn(stack) > 0);

        return state;
    }

    public static float impalingBonus(ItemStack stack, EntityType target, Era era) {
        if (!is(stack)) {
            return 0.0F;
        }
        int level = Impaling.levelOn(stack);
        if (level <= 0) {
            return 0.0F;
        }
        if (target == null) {
            return Impaling.bonusDamage(level, era);
        }
        return Impaling.bonusDamageAgainst(level, SensitiveToImpaling.contains(target, era), era);
    }

    public static float meleeDamage(ItemStack stack, EntityType target, Protocol protocol) {
        Era era = Era.enchantments(protocol);
        return (float) attackDamage() + impalingBonus(stack, target, era);
    }

    public static float thrownDamage(ItemStack stack, EntityType target, Protocol protocol) {
        Era era = Era.enchantments(protocol);
        return BASE_DAMAGE + impalingBonus(stack, target, era);
    }

    public static boolean strikesLightning(ItemStack stack, boolean thundering, boolean victimCanSeeSky) {
        if (!is(stack)) {
            return false;
        }
        return PostAttackEffects.channelingStrikesOnEntityHit(stack, thundering, victimCanSeeSky);
    }

    public static double expectedThrows(ItemStack stack, Protocol protocol) {
        Era era = Era.enchantments(protocol);
        return DurabilityModifiers.expectedUses(stack, MAX_DAMAGE, era);
    }

    public static float durabilitySkipChance(ItemStack stack, Protocol protocol) {
        Era era = Era.enchantments(protocol);
        return DurabilityModifiers.skipChance(stack, false, era);
    }

    public static boolean repairIsPossible(ItemStack stack, int observedRepair, int experienceAbsorbed) {
        return DurabilityModifiers.repairIsPossible(stack, observedRepair, experienceAbsorbed);
    }

    public static Reality launchImpulse(ItemStack stack, Protocol protocol) {
        TridentState state = resolve(stack, protocol);
        if (!state.propelsHolder()) {
            return Reality.impossible();
        }
        return Reality.of(state.spinAttackStrength());
    }

    public static Reality horizontalImpulse(ItemStack stack, float pitchDegrees, Protocol protocol) {
        Reality total = launchImpulse(stack, protocol);
        if (!total.possible()) {
            return total;
        }
        return Reality.of(total.bound() * Math.abs(Math.cos(Math.toRadians(pitchDegrees))));
    }

    public static Reality verticalImpulse(ItemStack stack, float pitchDegrees, boolean onGround, Protocol protocol) {
        Reality total = launchImpulse(stack, protocol);
        if (!total.possible()) {
            return total;
        }

        double vertical = total.bound() * Math.abs(Math.sin(Math.toRadians(pitchDegrees)));
        if (onGround) {
            vertical += Riptide.GROUND_LAUNCH_LIFT;
        }
        return Reality.of(vertical);
    }

    public static Reality returnSpeed(ItemStack stack, Protocol protocol) {
        TridentState state = resolve(stack, protocol);
        if (state.returnAcceleration() <= 0.0D) {
            return Reality.impossible();
        }
        return Reality.of(state.returnAcceleration());
    }

    public static Reality damageFrom(ItemStack stack, EntityType target, Protocol protocol) {
        return Reality.of(meleeDamage(stack, target, protocol));
    }

    public static Reality durabilityFrom(int damage) {
        int left = MAX_DAMAGE - damage;
        return Reality.of(left < 0 ? 0 : left);
    }

    public static Reality durabilityAfter(int currentDamage, int cost, Protocol protocol) {
        return Reality.of(TridentDurability.remainingAfter(currentDamage, cost, protocol));
    }

    public static Reality useDurationFrom(int ticksHeld) {
        return Reality.of(TridentUse.USE_DURATION - ticksHeld);
    }
}

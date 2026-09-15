package teacommontea.veritechasse.vanilla.Item.Trident;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Channeling;
import teacommontea.veritechasse.vanilla.Enchantments.Impaling;
import teacommontea.veritechasse.vanilla.Enchantments.Loyalty;
import teacommontea.veritechasse.vanilla.Enchantments.Mending;
import teacommontea.veritechasse.vanilla.Enchantments.Riptide;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Exclusivity;
import teacommontea.veritechasse.vanilla.Enchantments.Unbreaking;
import teacommontea.veritechasse.vanilla.Enchantments.VanishingCurse;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class Trident {

    public static final int THROW_THRESHOLD_TIME = 10;
    public static final float BASE_DAMAGE = 8.0F;
    public static final float PROJECTILE_SHOOT_POWER = 2.5F;

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

    public static TridentState resolve(ItemStack stack, Protocol protocol) {
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

        state.addBonusDamage(Impaling.bonusDamage(Impaling.levelOn(stack), era));
        state.summonsLightning(Channeling.levelOn(stack) > 0);

        return state;
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
}

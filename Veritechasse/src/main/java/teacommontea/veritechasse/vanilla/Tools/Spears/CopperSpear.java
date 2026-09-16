package teacommontea.veritechasse.Vanilla.Tools.Spears;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Spears.Support.KineticCondition;
import teacommontea.veritechasse.Vanilla.Tools.Spears.Support.Spear;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;

public final class CopperSpear {

    public static final String KEY = "copper_spear";
    public static final Material MATERIAL = Materials.COPPER;

    public static final int MAX_DAMAGE = 190;
    public static final int ENCHANTMENT_VALUE = 13;

    public static final float ATTACK_DURATION = 0.85F;
    public static final float DAMAGE_MULTIPLIER = 0.82F;
    public static final float DELAY = 0.65F;

    public static final float DISMOUNT_TIME = 4.0F;
    public static final float DISMOUNT_THRESHOLD = 12.0F;
    public static final float KNOCKBACK_TIME = 8.25F;
    public static final float KNOCKBACK_THRESHOLD = 5.1F;
    public static final float DAMAGE_TIME = 12.5F;
    public static final float DAMAGE_THRESHOLD = 4.6F;

    public static final boolean FIRE_RESISTANT = false;

    public static final KineticCondition DISMOUNT = KineticCondition.ofAttackerSpeed(DISMOUNT_TIME, DISMOUNT_THRESHOLD);
    public static final KineticCondition KNOCKBACK = KineticCondition.ofAttackerSpeed(KNOCKBACK_TIME, KNOCKBACK_THRESHOLD);
    public static final KineticCondition DAMAGE = KineticCondition.ofRelativeSpeed(DAMAGE_TIME, DAMAGE_THRESHOLD);

    private CopperSpear() {
    }

    public static double attackDamageBase() {
        return Spear.attackDamageBase(MATERIAL);
    }

    public static double attackSpeed() {
        return Spear.attackSpeed(ATTACK_DURATION);
    }

    public static int delayTicks() {
        return Spear.delayTicks(DELAY);
    }

    public static float damage(double relativeSpeed) {
        return Spear.damage(attackDamageBase(), relativeSpeed, DAMAGE_MULTIPLIER);
    }

    public static boolean dealsDamage(int ticksUsed, double attackerSpeed, double relativeSpeed, boolean isPlayer) {
        return DAMAGE.test(ticksUsed, attackerSpeed, relativeSpeed, Spear.actionFactor(isPlayer));
    }

    public static boolean dealsKnockback(int ticksUsed, double attackerSpeed, double relativeSpeed, boolean isPlayer) {
        return KNOCKBACK.test(ticksUsed, attackerSpeed, relativeSpeed, Spear.actionFactor(isPlayer));
    }

    public static boolean dealsDismount(int ticksUsed, double attackerSpeed, double relativeSpeed, boolean isPlayer) {
        return DISMOUNT.test(ticksUsed, attackerSpeed, relativeSpeed, Spear.actionFactor(isPlayer));
    }

    public static Reality damageFrom(double relativeSpeed) {
        return Reality.of(damage(relativeSpeed));
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

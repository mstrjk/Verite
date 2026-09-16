package teacommontea.veritechasse.Vanilla.Tools.Spears;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Spears.Support.KineticCondition;
import teacommontea.veritechasse.Vanilla.Tools.Spears.Support.Spear;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;

public final class NetheriteSpear {

    public static final String KEY = "netherite_spear";
    public static final Material MATERIAL = Materials.NETHERITE;

    public static final int MAX_DAMAGE = 2031;
    public static final int ENCHANTMENT_VALUE = 15;

    public static final float ATTACK_DURATION = 1.15F;
    public static final float DAMAGE_MULTIPLIER = 1.2F;
    public static final float DELAY = 0.4F;

    public static final float DISMOUNT_TIME = 2.5F;
    public static final float DISMOUNT_THRESHOLD = 9.0F;
    public static final float KNOCKBACK_TIME = 5.5F;
    public static final float KNOCKBACK_THRESHOLD = 5.1F;
    public static final float DAMAGE_TIME = 8.75F;
    public static final float DAMAGE_THRESHOLD = 4.6F;

    public static final boolean FIRE_RESISTANT = true;

    public static final KineticCondition DISMOUNT = KineticCondition.ofAttackerSpeed(DISMOUNT_TIME, DISMOUNT_THRESHOLD);
    public static final KineticCondition KNOCKBACK = KineticCondition.ofAttackerSpeed(KNOCKBACK_TIME, KNOCKBACK_THRESHOLD);
    public static final KineticCondition DAMAGE = KineticCondition.ofRelativeSpeed(DAMAGE_TIME, DAMAGE_THRESHOLD);

    private NetheriteSpear() {
    }

    public static boolean survivesLava() {
        return FIRE_RESISTANT;
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

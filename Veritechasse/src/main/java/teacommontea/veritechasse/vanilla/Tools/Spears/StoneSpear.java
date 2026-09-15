package teacommontea.veritechasse.vanilla.Tools.Spears;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Spears.Support.KineticCondition;
import teacommontea.veritechasse.vanilla.Tools.Spears.Support.Spear;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;

public final class StoneSpear {

    public static final String KEY = "stone_spear";
    public static final Material MATERIAL = Materials.STONE;

    public static final int MAX_DAMAGE = 131;
    public static final int ENCHANTMENT_VALUE = 5;

    public static final float ATTACK_DURATION = 0.75F;
    public static final float DAMAGE_MULTIPLIER = 0.82F;
    public static final float DELAY = 0.7F;

    public static final float DISMOUNT_TIME = 4.5F;
    public static final float DISMOUNT_THRESHOLD = 13.0F;
    public static final float KNOCKBACK_TIME = 9.0F;
    public static final float KNOCKBACK_THRESHOLD = 5.1F;
    public static final float DAMAGE_TIME = 13.75F;
    public static final float DAMAGE_THRESHOLD = 4.6F;

    public static final boolean FIRE_RESISTANT = false;

    public static final KineticCondition DISMOUNT = KineticCondition.ofAttackerSpeed(DISMOUNT_TIME, DISMOUNT_THRESHOLD);
    public static final KineticCondition KNOCKBACK = KineticCondition.ofAttackerSpeed(KNOCKBACK_TIME, KNOCKBACK_THRESHOLD);
    public static final KineticCondition DAMAGE = KineticCondition.ofRelativeSpeed(DAMAGE_TIME, DAMAGE_THRESHOLD);

    private StoneSpear() {
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

package teacommontea.veritechasse.vanilla.Tools.Swords.Support;

public final class AttackStrength {

    public static final float BASE_FACTOR = 0.2F;
    public static final float SCALED_FACTOR = 0.8F;
    public static final float FULL_STRENGTH_THRESHOLD = 0.9F;
    public static final float CRIT_MULTIPLIER = 1.5F;
    public static final float KNOCKBACK_BONUS = 0.5F;
    public static final float ATTACK_EXHAUSTION = 0.1F;

    private AttackStrength() {
    }

    public static float scale(int ticksSinceLastAttack, double attackSpeed, float partialTick) {
        double delay = cooldownTicks(attackSpeed);
        if (delay <= 0.0D) {
            return 1.0F;
        }
        double raw = ((double) ticksSinceLastAttack + (double) partialTick) / delay;
        if (raw < 0.0D) {
            return 0.0F;
        }
        return raw > 1.0D ? 1.0F : (float) raw;
    }

    public static double cooldownTicks(double attackSpeed) {
        if (attackSpeed <= 0.0D) {
            return 0.0D;
        }
        return 1.0D / attackSpeed * 20.0D;
    }

    public static float damageScaleFactor(float attackStrengthScale) {
        return BASE_FACTOR + attackStrengthScale * attackStrengthScale * SCALED_FACTOR;
    }

    public static boolean fullStrength(float attackStrengthScale) {
        return attackStrengthScale > FULL_STRENGTH_THRESHOLD;
    }

    public static boolean isKnockbackAttack(boolean sprinting, boolean fullStrength) {
        return sprinting && fullStrength;
    }
}

package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class Knockback {

    public static final double EXISTING_MOMENTUM_RETAINED = 0.5D;
    public static final double VERTICAL_CAP_ON_GROUND = 0.4D;
    public static final double MINIMUM_DIRECTION_LENGTH_SQR = 1.0E-5D;
    public static final double RANDOM_DIRECTION_SCALE = 0.01D;

    public static final double DEFAULT_KNOCKBACK = 0.4D;
    public static final double SPRINT_BONUS_POWER = 1.0D;

    public static final double ARROW_KNOCKBACK_SCALE = 0.6D;

    public static final double FISHING_ROD_PULL_SCALE = 0.1D;

    private Knockback() {
    }

    public static double afterResistance(double power, double knockbackResistance) {
        double resisted = power * (1.0D - knockbackResistance);
        return resisted < 0.0D ? 0.0D : resisted;
    }

    public static double horizontalAfter(double currentHorizontal, double power, double knockbackResistance) {
        double effective = afterResistance(power, knockbackResistance);
        if (effective <= 0.0D) {
            return currentHorizontal;
        }
        return currentHorizontal * EXISTING_MOMENTUM_RETAINED + effective;
    }

    public static double maximumHorizontal(double currentHorizontal, double power, double knockbackResistance) {
        return horizontalAfter(currentHorizontal, power, knockbackResistance);
    }

    public static double verticalAfter(double currentVertical, double power, boolean onGround) {
        if (!onGround) {
            return currentVertical;
        }
        double raised = currentVertical * EXISTING_MOMENTUM_RETAINED + power;
        return raised > VERTICAL_CAP_ON_GROUND ? VERTICAL_CAP_ON_GROUND : raised;
    }

    public static double meleeTotalPower(double attackKnockbackAttribute) {
        return DEFAULT_KNOCKBACK + attackKnockbackAttribute;
    }

    public static double horizontalAfterMelee(
            double currentHorizontal,
            double attackKnockbackAttribute,
            double knockbackResistance) {
        double afterBase = horizontalAfter(currentHorizontal, DEFAULT_KNOCKBACK, knockbackResistance);
        if (attackKnockbackAttribute <= 0.0D) {
            return afterBase;
        }
        return horizontalAfter(afterBase, attackKnockbackAttribute, knockbackResistance);
    }

    public static double arrowKnockback(double knockbackValue, double knockbackResistance) {
        double resistance = 1.0D - knockbackResistance;
        if (resistance < 0.0D) {
            resistance = 0.0D;
        }
        return knockbackValue * ARROW_KNOCKBACK_SCALE * resistance;
    }

    public static double fishingRodPull(double ownerCoordinate, double hookCoordinate) {
        return (ownerCoordinate - hookCoordinate) * FISHING_ROD_PULL_SCALE;
    }

    public static double fishingRodPullMagnitude(double distanceToOwner) {
        return distanceToOwner * FISHING_ROD_PULL_SCALE;
    }

    public static Reality horizontalFrom(double currentHorizontal, double power, double knockbackResistance) {
        return Reality.of(horizontalAfter(currentHorizontal, power, knockbackResistance));
    }

    public static Reality arrowFrom(double knockbackValue, double knockbackResistance) {
        return Reality.of(arrowKnockback(knockbackValue, knockbackResistance));
    }
}

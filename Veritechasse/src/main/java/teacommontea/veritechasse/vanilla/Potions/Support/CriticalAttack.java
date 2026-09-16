package teacommontea.veritechasse.Vanilla.Potions.Support;

public final class CriticalAttack {

    public static final float DAMAGE_MULTIPLIER = 1.5F;

    private CriticalAttack() {
    }

    public static boolean permitted(
        double fallDistance,
        boolean onGround,
        boolean onClimbable,
        boolean inWater,
        boolean mobilityRestricted,
        boolean passenger,
        boolean targetIsLiving,
        boolean sprinting
    ) {
        return fallDistance > 0.0D
            && !onGround
            && !onClimbable
            && !inWater
            && !mobilityRestricted
            && !passenger
            && targetIsLiving
            && !sprinting;
    }

    public static float apply(float damage) {
        return damage * DAMAGE_MULTIPLIER;
    }
}

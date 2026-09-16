package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerMisc;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Reality;

public final class MiscReality {

    public static final double TOLERANCE = 1.0E-9D;

    public static final double DEATH_IMPULSE_VERTICAL = 0.1D;
    public static final double DEATH_IMPULSE_HORIZONTAL = 0.1D;

    private MiscReality() {
    }

    public static double maximumFlightHorizontal(float flyingSpeed, boolean sprinting) {
        return CreativeFlight.horizontalTerminal(flyingSpeed, sprinting);
    }

    public static double maximumFlightHorizontal(
            double carriedHorizontal,
            float flyingSpeed,
            boolean sprinting) {
        double decayed = CreativeFlight.horizontalAfterTick(
            carriedHorizontal, flyingSpeed, sprinting);
        double steady = CreativeFlight.horizontalTerminal(flyingSpeed, sprinting);
        return decayed > steady ? decayed : steady;
    }

    public static double maximumFlightHorizontal(
            double carriedHorizontal,
            float flyingSpeed,
            boolean sprinting,
            boolean inWater,
            boolean inLava) {
        double decayed = CreativeFlight.horizontalAfterTick(
            carriedHorizontal, flyingSpeed, sprinting, inWater, inLava);
        double steady = CreativeFlight.horizontalTerminal(
            flyingSpeed, sprinting, inWater, inLava);
        return decayed > steady ? decayed : steady;
    }

    public static float flightAcceleration(float flyingSpeed, boolean sprinting) {
        return CreativeFlight.horizontalSpeed(flyingSpeed, sprinting);
    }

    public static boolean permitsFlightHorizontal(
            double observedHorizontal,
            float flyingSpeed,
            boolean sprinting) {
        return observedHorizontal
            <= maximumFlightHorizontal(flyingSpeed, sprinting) + TOLERANCE;
    }

    public static boolean permitsFlightVertical(double observedDeltaY, float flyingSpeed) {
        return Math.abs(observedDeltaY)
            <= CreativeFlight.maximumAscent(flyingSpeed) + TOLERANCE;
    }

    public static boolean fliesWithoutPermission(boolean claimsFlying, boolean mayFly) {
        return claimsFlying && !mayFly;
    }

    public static double maximumSwimSteering(double deltaY, double lookAngleY) {
        return SwimSteering.maximumAscent(lookAngleY, deltaY);
    }

    public static boolean permitsSwimSteering(
            double observedDeltaY,
            double deltaY,
            double lookAngleY) {
        return observedDeltaY <= maximumSwimSteering(deltaY, lookAngleY) + TOLERANCE;
    }

    public static double maximumSpinAttackVertical(
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            boolean onGround,
            Era era) {
        double impulse = Math.abs(
            SpinAttack.verticalImpulse(trident, yawDegrees, pitchDegrees, era));
        return impulse + SpinAttack.groundLaunchDisplacement(onGround);
    }

    public static boolean permitsSpinAttack(
            double observedDeltaY,
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            boolean onGround,
            Era era) {
        double bound = maximumSpinAttackVertical(trident, yawDegrees, pitchDegrees, onGround, era);
        return Math.abs(observedDeltaY) <= bound + TOLERANCE;
    }

    public static boolean spinAttackWithoutTrident(
            boolean claimsSpinning,
            ItemStack trident,
            boolean inWaterOrRain,
            boolean passenger) {
        if (!claimsSpinning) {
            return false;
        }
        return !SpinAttack.canLaunch(trident, inWaterOrRain, passenger);
    }

    public static double deathImpulseVertical() {
        return DEATH_IMPULSE_VERTICAL;
    }

    public static double deathImpulseHorizontal(boolean hasDamageSource) {
        return hasDamageSource ? DEATH_IMPULSE_HORIZONTAL : 0.0D;
    }

    public static boolean permitsDeathImpulse(double observedHorizontal, boolean hasDamageSource) {
        return observedHorizontal <= deathImpulseHorizontal(hasDamageSource) + TOLERANCE;
    }

    public static Reality flightFrom(float flyingSpeed, boolean sprinting) {
        return Reality.of(maximumFlightHorizontal(flyingSpeed, sprinting));
    }

    public static Reality spinAttackFrom(
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            boolean onGround,
            Era era) {
        return Reality.of(
            maximumSpinAttackVertical(trident, yawDegrees, pitchDegrees, onGround, era));
    }
}

package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerSneak;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class SneakPose {

    public static final String STANDING = "standing";
    public static final String CROUCHING = "crouching";
    public static final String SWIMMING = "swimming";
    public static final String FALL_FLYING = "fall_flying";
    public static final String SPIN_ATTACK = "spin_attack";
    public static final String SLEEPING = "sleeping";

    public static final float WIDTH = 0.6F;

    public static final float STANDING_HEIGHT = 1.8F;
    public static final float CROUCHING_HEIGHT = 1.5F;
    public static final float SWIMMING_HEIGHT = 0.6F;

    public static final float STANDING_EYE_HEIGHT = 1.62F;
    public static final float CROUCHING_EYE_HEIGHT = 1.27F;
    public static final float SWIMMING_EYE_HEIGHT = 0.4F;

    public static final int EXPLICIT_EYE_HEIGHT_PROTOCOL_MAJOR = 1;
    public static final int EXPLICIT_EYE_HEIGHT_PROTOCOL_MINOR = 20;
    public static final int EXPLICIT_EYE_HEIGHT_PROTOCOL_PATCH = 5;

    private SneakPose() {
    }

    public static String normalise(String pose) {
        return pose == null ? null : pose.toLowerCase(Locale.ROOT);
    }

    public static float heightOf(String pose) {
        String key = normalise(pose);
        if (CROUCHING.equals(key)) {
            return CROUCHING_HEIGHT;
        }
        if (SWIMMING.equals(key) || FALL_FLYING.equals(key) || SPIN_ATTACK.equals(key)) {
            return SWIMMING_HEIGHT;
        }
        return STANDING_HEIGHT;
    }

    public static float eyeHeightOf(String pose) {
        String key = normalise(pose);
        if (CROUCHING.equals(key)) {
            return CROUCHING_EYE_HEIGHT;
        }
        if (SWIMMING.equals(key) || FALL_FLYING.equals(key) || SPIN_ATTACK.equals(key)) {
            return SWIMMING_EYE_HEIGHT;
        }
        return STANDING_EYE_HEIGHT;
    }

    public static String desiredPose(
            boolean sleeping,
            boolean swimming,
            boolean fallFlying,
            boolean autoSpinAttack,
            boolean shiftKeyDown,
            boolean flying) {
        if (sleeping) {
            return SLEEPING;
        }
        if (swimming) {
            return SWIMMING;
        }
        if (fallFlying) {
            return FALL_FLYING;
        }
        if (autoSpinAttack) {
            return SPIN_ATTACK;
        }
        return SneakState.desiresCrouch(shiftKeyDown, flying) ? CROUCHING : STANDING;
    }

    public static String resolvedPose(
            String desiredPose,
            boolean spectator,
            boolean passenger,
            boolean fitsDesired,
            boolean fitsCrouching) {
        if (spectator || passenger || fitsDesired) {
            return normalise(desiredPose);
        }
        if (fitsCrouching) {
            return CROUCHING;
        }
        return SWIMMING;
    }

    public static boolean forcedIntoCrouch(String resolvedPose, boolean shiftKeyDown) {
        return CROUCHING.equals(normalise(resolvedPose)) && !shiftKeyDown;
    }

    public static boolean isCrouching(String pose) {
        return CROUCHING.equals(normalise(pose));
    }

    public static boolean poseIsPossible(
            String claimedPose,
            boolean fitsStanding,
            boolean fitsCrouching) {
        String key = normalise(claimedPose);
        if (STANDING.equals(key)) {
            return fitsStanding;
        }
        if (CROUCHING.equals(key)) {
            return fitsCrouching;
        }
        return true;
    }

    public static boolean eyeHeightIsExplicit(Protocol protocol) {
        return protocol.atLeast(
            EXPLICIT_EYE_HEIGHT_PROTOCOL_MAJOR,
            EXPLICIT_EYE_HEIGHT_PROTOCOL_MINOR,
            EXPLICIT_EYE_HEIGHT_PROTOCOL_PATCH);
    }

    public static Reality heightFrom(String pose) {
        return Reality.of(heightOf(pose));
    }
}

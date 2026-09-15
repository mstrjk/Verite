package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerCrawl;

import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerSneak.SneakPose;
import teacommontea.veritechasse.vanilla.Protocol;

public final class CrawlState {

    public static final float HEIGHT = 0.6F;
    public static final float WIDTH = 0.6F;
    public static final float EYE_HEIGHT = 0.4F;

    public static final boolean REQUIRES_NO_KEYBIND = true;

    public static final int POSE_CHECK_RENAME_PROTOCOL_MAJOR = 1;
    public static final int POSE_CHECK_RENAME_PROTOCOL_MINOR = 20;
    public static final int POSE_CHECK_RENAME_PROTOCOL_PATCH = 2;

    private CrawlState() {
    }

    public static boolean swimmingFlagSet(
            boolean currentlySwimming,
            boolean sprinting,
            boolean inWater,
            boolean underWater,
            boolean passenger,
            boolean flying) {
        if (flying) {
            return false;
        }
        if (passenger || !sprinting) {
            return false;
        }
        return currentlySwimming ? inWater : underWater;
    }

    public static boolean isCrawling(String resolvedPose, boolean inWater) {
        return SneakPose.SWIMMING.equals(SneakPose.normalise(resolvedPose)) && !inWater;
    }

    public static boolean isVisuallySwimming(String resolvedPose) {
        return SneakPose.SWIMMING.equals(SneakPose.normalise(resolvedPose));
    }

    public static boolean forcedByGeometry(
            boolean fitsDesired,
            boolean fitsCrouching,
            boolean spectator,
            boolean passenger) {
        if (spectator || passenger) {
            return false;
        }
        return !fitsDesired && !fitsCrouching;
    }

    public static boolean poseUpdatesAtAll(boolean fitsSwimming) {
        return fitsSwimming;
    }

    public static boolean poseCheckWasRenamed(Protocol protocol) {
        return protocol.atLeast(
            POSE_CHECK_RENAME_PROTOCOL_MAJOR,
            POSE_CHECK_RENAME_PROTOCOL_MINOR,
            POSE_CHECK_RENAME_PROTOCOL_PATCH);
    }

    public static boolean isMovingSlowly(boolean crawling) {
        return crawling;
    }
}

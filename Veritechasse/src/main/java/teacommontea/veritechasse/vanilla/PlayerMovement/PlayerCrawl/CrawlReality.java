package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerCrawl;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerSneak.SneakPose;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerSneak.SneakReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerSneak.SneakSpeed;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Reality;

public final class CrawlReality {

    private CrawlReality() {
    }

    public static double maximumSpeed(
            ActiveEffects effects,
            String blockBelow,
            String blockHere,
            ItemStack leggings,
            Era era) {
        return SneakReality.maximumTerminalSpeed(
            effects, blockBelow, blockHere, false, true, leggings, era);
    }

    public static boolean permitsSpeed(
            double observedHorizontal,
            ActiveEffects effects,
            String blockBelow,
            String blockHere,
            ItemStack leggings,
            Era era) {
        return SneakReality.permitsSpeed(
            observedHorizontal, effects, blockBelow, blockHere, false, true, leggings, era);
    }

    public static double speedFactor(ItemStack leggings, Era era) {
        return SneakSpeed.factorFor(leggings, era);
    }

    public static boolean poseIsJustified(
            String resolvedPose,
            boolean inWater,
            boolean fitsStanding,
            boolean fitsCrouching,
            boolean spectator,
            boolean passenger) {
        if (!CrawlState.isCrawling(resolvedPose, inWater)) {
            return true;
        }
        return CrawlState.forcedByGeometry(fitsStanding, fitsCrouching, spectator, passenger);
    }

    public static boolean claimsCrawlWithHeadroom(
            String resolvedPose,
            boolean inWater,
            boolean fitsStanding,
            boolean fitsCrouching,
            boolean spectator,
            boolean passenger) {
        return !poseIsJustified(
            resolvedPose, inWater, fitsStanding, fitsCrouching, spectator, passenger);
    }

    public static boolean swimmingFlagWithoutWater(boolean swimmingFlag, boolean inWater) {
        return swimmingFlag && !inWater;
    }

    public static boolean poseIsStale(boolean fitsSwimming) {
        return !CrawlState.poseUpdatesAtAll(fitsSwimming);
    }

    public static float heightOf(String resolvedPose) {
        return SneakPose.heightOf(resolvedPose);
    }

    public static Reality speedFrom(
            ActiveEffects effects,
            String blockBelow,
            String blockHere,
            ItemStack leggings,
            Era era) {
        return Reality.of(maximumSpeed(effects, blockBelow, blockHere, leggings, era));
    }
}

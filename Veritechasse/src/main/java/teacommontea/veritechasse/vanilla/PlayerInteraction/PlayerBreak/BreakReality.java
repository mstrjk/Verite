package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Support.DestroySpeed;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class BreakReality {

    public static final double TOLERANCE = 1.0E-6D;

    private BreakReality() {
    }

    public static float perTickProgress(
            float baseToolSpeed,
            float blockDestroySpeed,
            boolean hasCorrectTool,
            float miningEfficiency,
            int hasteAmplifier,
            int conduitPowerAmplifier,
            int miningFatigueAmplifier,
            float blockBreakSpeed,
            boolean eyeInWater,
            float submergedMiningSpeed,
            boolean aquaAffinity,
            boolean onGround,
            ToolEra era) {
        float playerSpeed = DestroySpeed.resolve(
            baseToolSpeed,
            miningEfficiency,
            hasteAmplifier,
            conduitPowerAmplifier,
            miningFatigueAmplifier,
            blockBreakSpeed,
            eyeInWater,
            submergedMiningSpeed,
            aquaAffinity,
            onGround,
            era);
        return BreakProgress.perTickProgress(playerSpeed, blockDestroySpeed, hasCorrectTool);
    }

    public static float perTickProgressFor(
            float baseToolSpeed,
            float blockDestroySpeed,
            boolean hasCorrectTool,
            ItemStack tool,
            ItemStack helmet,
            int hasteAmplifier,
            int conduitPowerAmplifier,
            int miningFatigueAmplifier,
            float blockBreakSpeed,
            boolean eyeInWater,
            boolean onGround,
            ToolEra toolEra,
            Era enchantEra) {
        float playerSpeed = DestroySpeed.resolveFor(
            baseToolSpeed,
            tool,
            helmet,
            hasteAmplifier,
            conduitPowerAmplifier,
            miningFatigueAmplifier,
            blockBreakSpeed,
            eyeInWater,
            onGround,
            toolEra,
            enchantEra);
        return BreakProgress.perTickProgress(playerSpeed, blockDestroySpeed, hasCorrectTool);
    }

    public static int minimumTicks(float perTickProgress) {
        return BreakProgress.minimumTicksForServerBreak(perTickProgress);
    }

    public static boolean brokeTooFast(float perTickProgress, int observedTicks) {
        return BreakProgress.breakWasTooFast(perTickProgress, observedTicks);
    }

    public static boolean targetWasAlreadyAir(String serverBlockName) {
        return BlockKnowledge.breakingAlreadyBrokenBlock(serverBlockName);
    }

    public static boolean outOfReach(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            double blockInteractionRange,
            boolean creative,
            Protocol protocol) {
        return !BlockReach.withinRange(
            eyeX, eyeY, eyeZ, blockX, blockY, blockZ,
            blockInteractionRange, creative, protocol);
    }

    public static boolean facingAwayFromTarget(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            float xRot,
            float yRot) {
        return !LookGeometry.facingCouldReach(
            eyeX, eyeY, eyeZ, blockX, blockY, blockZ, xRot, yRot);
    }

    public static boolean serverWouldAccept(
            boolean withinReach,
            boolean withinBuildHeight,
            boolean spawnProtected,
            boolean mayInteract,
            boolean actionRestricted,
            boolean targetIsAir) {
        if (!withinReach || !withinBuildHeight) {
            return false;
        }
        if (spawnProtected || !mayInteract || actionRestricted) {
            return false;
        }
        return !targetIsAir;
    }

    public static boolean instantBreak(
            float perTickProgress,
            boolean creative) {
        return creative || perTickProgress >= BreakProgress.INSTANT_BREAK_THRESHOLD;
    }

    public static Reality ticksFrom(float perTickProgress) {
        return BreakProgress.minimumTicksFrom(perTickProgress);
    }

    public static Reality reachFrom(
            double blockInteractionRange,
            boolean creative,
            Protocol protocol) {
        return BlockReach.rangeFrom(blockInteractionRange, creative, protocol);
    }

    public static Reality progressFrom(float perTickProgress, int observedTicks) {
        return BreakProgress.progressFrom(perTickProgress, observedTicks);
    }
}

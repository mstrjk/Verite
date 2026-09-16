package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerClimb;

import teacommontea.veritechasse.vanilla.Potions.Levitation;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class ClimbMotion {

    public static final float CLAMP_SOURCE = 0.15F;
    public static final double CLAMP = CLAMP_SOURCE;

    public static final double COLLISION_BOOST = 0.2D;

    public static final double SUPPRESSED_DESCENT = 0.0D;

    public static final boolean RESETS_FALL_DISTANCE = true;

    public static final int GLIDE_THROUGH_PROTOCOL_MAJOR = 1;
    public static final int GLIDE_THROUGH_PROTOCOL_MINOR = 21;
    public static final int GLIDE_THROUGH_PROTOCOL_PATCH = 11;

    private ClimbMotion() {
    }

    public static boolean glideThroughApplies(Protocol protocol) {
        return protocol.atLeast(
            GLIDE_THROUGH_PROTOCOL_MAJOR,
            GLIDE_THROUGH_PROTOCOL_MINOR,
            GLIDE_THROUGH_PROTOCOL_PATCH);
    }

    public static boolean onClimbable(
            String blockAtFeet,
            boolean spectator,
            boolean fallFlying,
            boolean blockAllowsGlideThrough,
            boolean trapdoorIsLadderLike,
            Protocol protocol) {
        if (spectator) {
            return false;
        }
        if (fallFlying && glideThroughApplies(protocol) && blockAllowsGlideThrough) {
            return false;
        }
        if (ClimbableBlocks.contains(blockAtFeet)) {
            return true;
        }
        return ClimbableBlocks.isTrapdoor(blockAtFeet) && trapdoorIsLadderLike;
    }

    public static boolean trapdoorUsableAsLadder(
            boolean trapdoorOpen,
            String blockBelow,
            String trapdoorFacing,
            String ladderFacing) {
        if (!trapdoorOpen) {
            return false;
        }
        if (!ClimbableBlocks.isLadder(blockBelow)) {
            return false;
        }
        if (trapdoorFacing == null || ladderFacing == null) {
            return false;
        }
        return trapdoorFacing.equalsIgnoreCase(ladderFacing);
    }

    public static double clampHorizontal(double component) {
        if (component < -CLAMP) {
            return -CLAMP;
        }
        return component > CLAMP ? CLAMP : component;
    }

    public static boolean clampApplies(
            boolean onClimbable,
            boolean inWater,
            boolean inLava,
            boolean fallFlying,
            ActiveEffects effects) {
        if (!onClimbable) {
            return false;
        }
        if (inWater || inLava || fallFlying) {
            return false;
        }
        return effects == null
            || effects.amplifierOf(Levitation.KEY) == ActiveEffects.ABSENT;
    }

    public static double clampVerticalWhenApplicable(
            double deltaY,
            String blockAtFeet,
            boolean shiftKeyDown,
            boolean isPlayer,
            boolean inWater,
            boolean inLava,
            boolean fallFlying,
            ActiveEffects effects) {
        if (!clampApplies(true, inWater, inLava, fallFlying, effects)) {
            return deltaY;
        }
        return clampVertical(deltaY, blockAtFeet, shiftKeyDown, isPlayer);
    }

    public static double clampVertical(
            double deltaY,
            String blockAtFeet,
            boolean shiftKeyDown,
            boolean isPlayer) {
        double clamped = Math.max(deltaY, -CLAMP);
        if (clamped >= 0.0D) {
            return clamped;
        }
        if (!isPlayer || !shiftKeyDown) {
            return clamped;
        }
        if (ClimbableBlocks.isScaffolding(blockAtFeet)) {
            return clamped;
        }
        return SUPPRESSED_DESCENT;
    }

    public static double upwardAfterCollision(
            double deltaY,
            boolean horizontalCollision,
            boolean jumping,
            boolean onClimbable) {
        if (!onClimbable) {
            return deltaY;
        }
        if (!horizontalCollision && !jumping) {
            return deltaY;
        }
        return COLLISION_BOOST;
    }

    public static double upwardAfterCollisionOnLand(
            double deltaY,
            boolean horizontalCollision,
            boolean jumping,
            boolean onClimbable,
            boolean inPowderSnowWithLeatherBoots) {
        if (!onClimbable && !inPowderSnowWithLeatherBoots) {
            return deltaY;
        }
        if (!horizontalCollision && !jumping) {
            return deltaY;
        }
        return COLLISION_BOOST;
    }

    public static double upwardAfterCollisionInWater(
            double deltaY,
            boolean horizontalCollision,
            boolean onClimbable) {
        if (!onClimbable || !horizontalCollision) {
            return deltaY;
        }
        return COLLISION_BOOST;
    }

    public static boolean waterBranchIgnoresJumping() {
        return true;
    }

    public static double maximumAscent(boolean horizontalCollision, boolean jumping) {
        return horizontalCollision || jumping ? COLLISION_BOOST : CLAMP;
    }

    public static double maximumDescent() {
        return CLAMP;
    }

    public static boolean slideIsSuppressed(
            String blockAtFeet,
            boolean shiftKeyDown,
            boolean isPlayer) {
        if (!isPlayer || !shiftKeyDown) {
            return false;
        }
        return !ClimbableBlocks.isScaffolding(blockAtFeet);
    }

    public static Reality ascentFrom(boolean horizontalCollision, boolean jumping) {
        return Reality.of(maximumAscent(horizontalCollision, jumping));
    }

    public static Reality horizontalFrom() {
        return Reality.of(CLAMP);
    }
}

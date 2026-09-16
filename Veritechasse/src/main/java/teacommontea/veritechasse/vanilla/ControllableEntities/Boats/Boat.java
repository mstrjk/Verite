package teacommontea.veritechasse.Vanilla.ControllableEntities.Boats;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Boats.Support.BoatControl;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Boats.Support.BoatPhysics;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Boats.Support.BoatStatus;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Boats.Tags.BoatEntities;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.ExplosionKnockback;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.GroundFriction;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Boat {

    public static final String KEY = "boat";

    public static final double BOAT_RIDE_HEIGHT_DIVISOR = 3.0D;
    public static final double RAFT_RIDE_HEIGHT_FACTOR = 0.8888889D;

    public static final int MAX_PASSENGERS = 2;

    public static final boolean STEERABLE = true;
    public static final boolean CAN_JUMP = false;
    public static final boolean CAN_FLY = false;
    public static final boolean ACCEPTS_POTION_EFFECTS = false;
    public static final boolean CLIENT_AUTHORITATIVE_WHEN_RIDDEN = true;

    private Boat() {
    }

    public static boolean is(String entityName) {
        return BoatEntities.contains(entityName);
    }

    public static boolean isRaft(String entityName) {
        return BoatEntities.isRaft(entityName);
    }

    public static boolean isChested(String entityName) {
        return BoatEntities.isChested(entityName);
    }

    public static double rideHeight(double boxHeight, boolean raft) {
        if (raft) {
            return boxHeight * RAFT_RIDE_HEIGHT_FACTOR;
        }
        return boxHeight / BOAT_RIDE_HEIGHT_DIVISOR;
    }

    public static float maxAcceleration() {
        return BoatControl.maxAcceleration();
    }

    public static double maxVerticalFromBubbleColumn(boolean hasPlayer) {
        return BoatPhysics.bubbleColumnImpulse(false, hasPlayer);
    }

    public static boolean serverValidatesPosition(boolean playerControlled) {
        return BoatControl.serverValidatesPosition(playerControlled);
    }

    public static double horizontalAfterFriction(double horizontal, BoatStatus status, float landFriction) {
        float invFriction = status.usesLandFriction() ? landFriction : status.invFriction();
        return BoatPhysics.horizontalAfterFriction(horizontal, invFriction);
    }

    public static float groundFriction(String blockNameBelow) {
        return GroundFriction.of(blockNameBelow);
    }

    public static boolean onIce(String blockNameBelow) {
        return GroundFriction.isIce(blockNameBelow);
    }

    public static double terminalSpeedOnLand(String blockNameBelow) {
        return GroundFriction.terminalSpeedOn(blockNameBelow, BoatControl.FORWARD_ACCELERATION);
    }

    public static double knockbackFromExplosion(double distanceToCentre, float radius, float exposure) {
        return ExplosionKnockback.vehicleKnockbackPower(distanceToCentre, radius, exposure);
    }

    public static Reality explosionKnockbackFrom(double distanceToCentre, float radius, float exposure) {
        return ExplosionKnockback.knockbackFrom(distanceToCentre, radius, exposure);
    }

    public static boolean acceptsPotionEffects() {
        return ACCEPTS_POTION_EFFECTS;
    }

    public static Reality verticalControlFrom() {
        return BoatControl.verticalControlFrom();
    }

    public static Reality accelerationFrom(boolean inputUp, boolean inputDown, boolean inputLeft, boolean inputRight) {
        return BoatControl.accelerationFrom(inputUp, inputDown, inputLeft, inputRight);
    }
}

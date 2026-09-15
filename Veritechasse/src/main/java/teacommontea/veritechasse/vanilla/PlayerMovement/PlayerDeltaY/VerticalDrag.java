package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class VerticalDrag {

    public static final float BASE_VERTICAL_AIR_DRAG = 0.98F;
    public static final float OMNIDIRECTIONAL_AIR_DRAG = 0.91F;

    public static final boolean PLAYERS_ARE_OMNIDIRECTIONAL = false;

    public static final int MODIFIER_PROTOCOL_MAJOR = 26;
    public static final int MODIFIER_PROTOCOL_MINOR = 2;
    public static final int MODIFIER_PROTOCOL_PATCH = 0;

    private VerticalDrag() {
    }

    public static boolean modifierApplies(Protocol protocol) {
        return protocol.atLeast(
            MODIFIER_PROTOCOL_MAJOR,
            MODIFIER_PROTOCOL_MINOR,
            MODIFIER_PROTOCOL_PATCH);
    }

    public static float raw(boolean omnidirectionalAirMover) {
        return omnidirectionalAirMover ? OMNIDIRECTIONAL_AIR_DRAG : BASE_VERTICAL_AIR_DRAG;
    }

    public static float of(Protocol protocol, float airDragModifier, boolean omnidirectionalAirMover) {
        float base = raw(omnidirectionalAirMover);
        if (!modifierApplies(protocol)) {
            return base;
        }
        return GroundSpeed.computeModifiedFriction(base, airDragModifier);
    }

    public static float forPlayer(Protocol protocol, float airDragModifier) {
        return of(protocol, airDragModifier, PLAYERS_ARE_OMNIDIRECTIONAL);
    }

    public static float forPlayer() {
        return BASE_VERTICAL_AIR_DRAG;
    }

    public static double after(double deltaY, float drag) {
        return deltaY * (double) drag;
    }

    public static Reality dragFrom(Protocol protocol, float airDragModifier) {
        return Reality.of(forPlayer(protocol, airDragModifier));
    }
}

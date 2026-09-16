package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerEntry;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class LoginSequence {

    public static final String KEY = "login_sequence";

    public static final int MAX_TICKS_BEFORE_LOGIN = 600;

    public static final int CLIENT_LOADED_GATE_MAJOR = 1;
    public static final int CLIENT_LOADED_GATE_MINOR = 21;
    public static final int CLIENT_LOADED_GATE_PATCH = 4;

    private LoginSequence() {
    }

    public static boolean hasClientLoadedGate(Protocol protocol) {
        return protocol.atLeast(
            CLIENT_LOADED_GATE_MAJOR, CLIENT_LOADED_GATE_MINOR, CLIENT_LOADED_GATE_PATCH);
    }

    public static int afterTick(int tick) {
        return tick + 1;
    }

    public static boolean loginTimedOut(int tick) {
        return tick >= MAX_TICKS_BEFORE_LOGIN;
    }

    public static int ticksRemaining(int tick) {
        int remaining = MAX_TICKS_BEFORE_LOGIN - tick;
        return remaining <= 0 ? 0 : remaining;
    }

    public static boolean baselineExistsOnJoin() {
        return false;
    }

    public static boolean positionIsAuthoritativeOnJoin() {
        return true;
    }

    public static boolean movementIsEvaluated(boolean clientLoaded, Protocol protocol) {
        if (!hasClientLoadedGate(protocol)) {
            return true;
        }
        return clientLoaded;
    }

    public static Reality loginDeadline() {
        return Reality.of(MAX_TICKS_BEFORE_LOGIN);
    }
}

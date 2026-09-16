package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets;

import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.ClientInput;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class SneakSource {

    public static final String FROM_PLAYER_COMMAND = "player_command";
    public static final String FROM_CLIENT_INPUT = "client_input";
    public static final String UNAVAILABLE = "unavailable";

    private SneakSource() {
    }

    public static String of(Protocol protocol) {
        if (PlayerCommands.shiftKeyActionsExist(protocol)) {
            return FROM_PLAYER_COMMAND;
        }
        if (ClientInput.availableIn(protocol)) {
            return FROM_CLIENT_INPUT;
        }
        return UNAVAILABLE;
    }

    public static boolean readFromPlayerCommand(Protocol protocol) {
        return FROM_PLAYER_COMMAND.equals(of(protocol));
    }

    public static boolean readFromClientInput(Protocol protocol) {
        return FROM_CLIENT_INPUT.equals(of(protocol));
    }

    public static boolean isKnown(Protocol protocol) {
        return !UNAVAILABLE.equals(of(protocol));
    }

    public static boolean sneaking(
            Protocol protocol,
            String lastCommandAction,
            boolean clientInputShift) {
        if (readFromClientInput(protocol)) {
            return clientInputShift;
        }
        if (PlayerCommands.setsSneaking(lastCommandAction)) {
            return true;
        }
        if (PlayerCommands.clearsSneaking(lastCommandAction)) {
            return false;
        }
        return false;
    }
}

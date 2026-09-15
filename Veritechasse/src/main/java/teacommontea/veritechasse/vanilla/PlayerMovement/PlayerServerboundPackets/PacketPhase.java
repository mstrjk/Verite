package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets;

import java.util.Locale;

public enum PacketPhase {

    COMMON,
    CONFIGURATION,
    COOKIE,
    GAME,
    LOGIN,
    PING,
    STATUS;

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static PacketPhase of(String key) {
        if (key == null) {
            return null;
        }
        String wanted = key.toLowerCase(Locale.ROOT);
        for (PacketPhase phase : values()) {
            if (phase.key().equals(wanted)) {
                return phase;
            }
        }
        return null;
    }
}

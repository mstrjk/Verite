package teacommontea.api;


public enum PunishmentType {

    BAN,
    MUTE,
    WARNING,
    KICK;

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public static PunishmentType of(String s) {
        return valueOf(s.toUpperCase(java.util.Locale.ROOT));
    }
}

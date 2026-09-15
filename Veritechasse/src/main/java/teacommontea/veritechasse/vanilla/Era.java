package teacommontea.veritechasse.vanilla;

public enum Era {

    ENCHANTS_AS_CLASSES,
    ENCHANTS_AS_DATA;

    public static Era enchantments(Protocol protocol) {
        return protocol.atLeast(1, 21, 0) ? ENCHANTS_AS_DATA : ENCHANTS_AS_CLASSES;
    }
}

package teacommontea.veritesauver.invsee;

import java.util.UUID;


public final class SpectateTarget {

    private final UUID uuid;
    private final String name;

    private SpectateTarget(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public static SpectateTarget byName(String name) {
        return new SpectateTarget(null, name);
    }

    public static SpectateTarget byUuid(UUID uuid) {
        return new SpectateTarget(uuid, null);
    }

    public static SpectateTarget byProfile(UUID uuid, String name) {
        return new SpectateTarget(uuid, name);
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }
        return String.valueOf(uuid);
    }
}

package teacommontea.veritechasse.Vanilla.ControllableEntities.Boats.Support;

import java.util.Locale;

public enum BoatStatus {

    IN_WATER("in_water", 0.9F),
    UNDER_WATER("under_water", 0.45F),
    UNDER_FLOWING_WATER("under_flowing_water", 0.9F),
    IN_AIR("in_air", 0.9F),
    ON_LAND("on_land", 0.0F);

    public static final float DEFAULT_INV_FRICTION = 0.05F;

    private final String key;
    private final float invFriction;

    BoatStatus(String key, float invFriction) {
        this.key = key;
        this.invFriction = invFriction;
    }

    public String key() {
        return this.key;
    }

    public float invFriction() {
        return this.invFriction;
    }

    public boolean usesLandFriction() {
        return this == ON_LAND;
    }

    public boolean floats() {
        return this == IN_WATER;
    }

    public boolean submerged() {
        return this == UNDER_WATER || this == UNDER_FLOWING_WATER;
    }

    public static BoatStatus byKey(String key) {
        if (key == null) {
            return null;
        }
        String name = key.toLowerCase(Locale.ROOT);
        for (BoatStatus status : values()) {
            if (status.key.equals(name)) {
                return status;
            }
        }
        return null;
    }
}

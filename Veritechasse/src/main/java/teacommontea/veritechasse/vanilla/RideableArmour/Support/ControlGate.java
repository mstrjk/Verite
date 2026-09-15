package teacommontea.veritechasse.vanilla.RideableArmour.Support;

import java.util.Locale;

public enum ControlGate {

    SADDLE_ONLY("saddle_only"),
    SADDLE_AND_CARROT_ON_A_STICK("saddle_and_carrot_on_a_stick"),
    SADDLE_AND_WARPED_FUNGUS_ON_A_STICK("saddle_and_warped_fungus_on_a_stick"),
    HARNESS_AND_NOT_STILL("harness_and_not_still"),
    NONE("none");

    public static final String CARROT_ON_A_STICK = "carrot_on_a_stick";
    public static final String WARPED_FUNGUS_ON_A_STICK = "warped_fungus_on_a_stick";

    private final String key;

    ControlGate(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

    public boolean requiresSaddle() {
        return this == SADDLE_ONLY
            || this == SADDLE_AND_CARROT_ON_A_STICK
            || this == SADDLE_AND_WARPED_FUNGUS_ON_A_STICK;
    }

    public boolean requiresHarness() {
        return this == HARNESS_AND_NOT_STILL;
    }

    public String requiredHeldItem() {
        if (this == SADDLE_AND_CARROT_ON_A_STICK) {
            return CARROT_ON_A_STICK;
        }
        if (this == SADDLE_AND_WARPED_FUNGUS_ON_A_STICK) {
            return WARPED_FUNGUS_ON_A_STICK;
        }
        return null;
    }

    public boolean requiresHeldItem() {
        return requiredHeldItem() != null;
    }

    public boolean satisfiedBy(boolean equipped, String heldItemName, boolean stillTimeout) {
        if (this == NONE) {
            return false;
        }
        if (!equipped) {
            return false;
        }
        if (this == HARNESS_AND_NOT_STILL) {
            return !stillTimeout;
        }
        String required = requiredHeldItem();
        if (required == null) {
            return true;
        }
        return heldItemName != null && heldItemName.toLowerCase(Locale.ROOT).equals(required);
    }
}

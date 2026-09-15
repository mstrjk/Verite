package teacommontea.veritechasse.vanilla.PlayerArmour.Support;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.Protocol;

public enum ArmourType {

    HELMET("helmet", "head", 11),
    CHESTPLATE("chestplate", "chest", 16),
    LEGGINGS("leggings", "legs", 15),
    BOOTS("boots", "feet", 13),
    BODY("body", "body", 16);

    public static final int BODY_SLOT_PROTOCOL_MAJOR = 1;
    public static final int BODY_SLOT_PROTOCOL_MINOR = 21;
    public static final int BODY_SLOT_PROTOCOL_PATCH = 3;

    private final String key;
    private final String slotName;
    private final int unitDurability;

    ArmourType(String key, String slotName, int unitDurability) {
        this.key = key;
        this.slotName = slotName;
        this.unitDurability = unitDurability;
    }

    public String key() {
        return this.key;
    }

    public String slotName() {
        return this.slotName;
    }

    public int unitDurability() {
        return this.unitDurability;
    }

    public int durability(int durabilityMultiplier) {
        return this.unitDurability * durabilityMultiplier;
    }

    public boolean existsIn(Protocol protocol) {
        if (this != BODY) {
            return true;
        }
        return protocol.atLeast(BODY_SLOT_PROTOCOL_MAJOR, BODY_SLOT_PROTOCOL_MINOR, BODY_SLOT_PROTOCOL_PATCH);
    }

    public boolean isHumanoid() {
        return this != BODY;
    }

    public static ArmourType byKey(String key) {
        if (key == null) {
            return null;
        }
        String name = key.toLowerCase(Locale.ROOT);
        for (ArmourType type : values()) {
            if (type.key.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public static ArmourType bySlotName(String slotName) {
        if (slotName == null) {
            return null;
        }
        String name = slotName.toLowerCase(Locale.ROOT);
        for (ArmourType type : values()) {
            if (type.slotName.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public static ArmourType fromItemName(String itemName) {
        if (itemName == null) {
            return null;
        }
        String name = itemName.toLowerCase(Locale.ROOT);
        if (name.endsWith("_helmet")) {
            return HELMET;
        }
        if (name.endsWith("_chestplate")) {
            return CHESTPLATE;
        }
        if (name.endsWith("_leggings")) {
            return LEGGINGS;
        }
        if (name.endsWith("_boots")) {
            return BOOTS;
        }
        if (name.endsWith("_horse_armor") || name.endsWith("_armor") || name.equals("harness")) {
            return BODY;
        }
        return null;
    }
}

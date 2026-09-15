package teacommontea.veritechasse.vanilla.PlayerArmour.Support;

public final class Material {

    private final String key;
    private final int durabilityMultiplier;
    private final int defenceBoots;
    private final int defenceLeggings;
    private final int defenceChestplate;
    private final int defenceHelmet;
    private final int defenceBody;
    private final int enchantmentValue;
    private final float toughness;
    private final float knockbackResistance;

    public Material(
            String key,
            int durabilityMultiplier,
            int defenceBoots,
            int defenceLeggings,
            int defenceChestplate,
            int defenceHelmet,
            int defenceBody,
            int enchantmentValue,
            float toughness,
            float knockbackResistance) {
        this.key = key;
        this.durabilityMultiplier = durabilityMultiplier;
        this.defenceBoots = defenceBoots;
        this.defenceLeggings = defenceLeggings;
        this.defenceChestplate = defenceChestplate;
        this.defenceHelmet = defenceHelmet;
        this.defenceBody = defenceBody;
        this.enchantmentValue = enchantmentValue;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    public String key() {
        return this.key;
    }

    public int durabilityMultiplier() {
        return this.durabilityMultiplier;
    }

    public int defenceBoots() {
        return this.defenceBoots;
    }

    public int defenceLeggings() {
        return this.defenceLeggings;
    }

    public int defenceChestplate() {
        return this.defenceChestplate;
    }

    public int defenceHelmet() {
        return this.defenceHelmet;
    }

    public int defenceBody() {
        return this.defenceBody;
    }

    public int enchantmentValue() {
        return this.enchantmentValue;
    }

    public float toughness() {
        return this.toughness;
    }

    public float knockbackResistance() {
        return this.knockbackResistance;
    }

    public int defenceFor(ArmourType type) {
        if (type == null) {
            return 0;
        }
        switch (type) {
            case BOOTS:
                return this.defenceBoots;
            case LEGGINGS:
                return this.defenceLeggings;
            case CHESTPLATE:
                return this.defenceChestplate;
            case HELMET:
                return this.defenceHelmet;
            case BODY:
                return this.defenceBody;
            default:
                return 0;
        }
    }

    public int durabilityFor(ArmourType type) {
        if (type == null) {
            return 0;
        }
        return type.durability(this.durabilityMultiplier);
    }
}

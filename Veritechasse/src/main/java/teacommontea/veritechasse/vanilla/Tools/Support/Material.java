package teacommontea.veritechasse.vanilla.Tools.Support;

public final class Material {

    private final String key;
    private final int legacyMiningLevel;
    private final int durability;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;

    public Material(String key, int legacyMiningLevel, int durability, float speed, float attackDamageBonus, int enchantmentValue) {
        this.key = key;
        this.legacyMiningLevel = legacyMiningLevel;
        this.durability = durability;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
    }

    public String key() {
        return this.key;
    }

    public int legacyMiningLevel() {
        return this.legacyMiningLevel;
    }

    public int durability() {
        return this.durability;
    }

    public float speed() {
        return this.speed;
    }

    public float attackDamageBonus() {
        return this.attackDamageBonus;
    }

    public int enchantmentValue() {
        return this.enchantmentValue;
    }
}

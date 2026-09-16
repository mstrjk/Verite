package teacommontea.veritechasse.Vanilla.Tools.Swords;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.Vanilla.Tools.Swords.Support.Sword;

public final class StoneSword {

    public static final String KEY = "stone_sword";
    public static final Material MATERIAL = Materials.STONE;

    public static final int MAX_DAMAGE = 131;
    public static final int ENCHANTMENT_VALUE = 5;
    public static final int MINING_TIER = 1;

    public static final boolean FIRE_RESISTANT = false;

    private StoneSword() {
    }

    public static double attackDamage() {
        return Sword.attackDamage(MATERIAL);
    }

    public static double attackSpeed() {
        return Sword.attackSpeed();
    }

    public static double attackCooldownTicks() {
        return Sword.attackCooldownTicks();
    }

    public static float destroySpeed(String blockName, boolean isPlantLike, ToolEra era) {
        return Sword.destroySpeed(blockName, isPlantLike, era);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

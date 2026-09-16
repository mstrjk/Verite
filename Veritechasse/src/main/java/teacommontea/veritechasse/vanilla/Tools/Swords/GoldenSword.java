package teacommontea.veritechasse.Vanilla.Tools.Swords;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.Vanilla.Tools.Swords.Support.Sword;

public final class GoldenSword {

    public static final String KEY = "golden_sword";
    public static final Material MATERIAL = Materials.GOLD;

    public static final int MAX_DAMAGE = 32;
    public static final int ENCHANTMENT_VALUE = 22;
    public static final int MINING_TIER = 0;

    public static final boolean FIRE_RESISTANT = false;

    private GoldenSword() {
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

package teacommontea.veritechasse.vanilla.Tools.Hoes;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Hoes.Support.Hoe;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class GoldenHoe {

    public static final String KEY = "golden_hoe";
    public static final Material MATERIAL = Materials.GOLD;

    public static final int MAX_DAMAGE = 32;
    public static final float MINING_SPEED = 12.0F;
    public static final int ENCHANTMENT_VALUE = 22;
    public static final int MINING_TIER = 0;

    public static final float ATTACK_DAMAGE_BASELINE = 0.0F;
    public static final float ATTACK_SPEED_BASELINE = -3.0F;

    public static final boolean FIRE_RESISTANT = false;

    private GoldenHoe() {
    }

    public static float baseDestroySpeed(String blockName, ToolEra era) {
        return Hoe.baseDestroySpeed(MATERIAL, blockName, era);
    }

    public static boolean correctForDrops(boolean requiresCorrectToolForDrops, String blockName, ToolEra era) {
        return Hoe.correctForDrops(MATERIAL, requiresCorrectToolForDrops, blockName, era);
    }

    public static double attackDamage() {
        return Hoe.attackDamage(MATERIAL, ATTACK_DAMAGE_BASELINE);
    }

    public static double attackSpeed() {
        return Hoe.attackSpeed(ATTACK_SPEED_BASELINE);
    }

    public static double attackCooldownTicks() {
        return Hoe.attackCooldownTicks(ATTACK_SPEED_BASELINE);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

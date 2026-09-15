package teacommontea.veritechasse.vanilla.Tools.Hoes;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Hoes.Support.Hoe;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class StoneHoe {

    public static final String KEY = "stone_hoe";
    public static final Material MATERIAL = Materials.STONE;

    public static final int MAX_DAMAGE = 131;
    public static final float MINING_SPEED = 4.0F;
    public static final int ENCHANTMENT_VALUE = 5;
    public static final int MINING_TIER = 1;

    public static final float ATTACK_DAMAGE_BASELINE = -1.0F;
    public static final float ATTACK_SPEED_BASELINE = -2.0F;

    public static final boolean FIRE_RESISTANT = false;

    private StoneHoe() {
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

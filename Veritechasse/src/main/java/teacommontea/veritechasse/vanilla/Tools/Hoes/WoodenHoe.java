package teacommontea.veritechasse.Vanilla.Tools.Hoes;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Hoes.Support.Hoe;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class WoodenHoe {

    public static final String KEY = "wooden_hoe";
    public static final Material MATERIAL = Materials.WOOD;

    public static final int MAX_DAMAGE = 59;
    public static final float MINING_SPEED = 2.0F;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final int MINING_TIER = 0;

    public static final float ATTACK_DAMAGE_BASELINE = 0.0F;
    public static final float ATTACK_SPEED_BASELINE = -3.0F;

    public static final boolean FIRE_RESISTANT = false;

    private WoodenHoe() {
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

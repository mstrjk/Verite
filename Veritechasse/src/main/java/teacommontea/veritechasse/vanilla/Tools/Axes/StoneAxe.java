package teacommontea.veritechasse.Vanilla.Tools.Axes;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Axes.Support.Axe;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class StoneAxe {

    public static final String KEY = "stone_axe";
    public static final Material MATERIAL = Materials.STONE;

    public static final int MAX_DAMAGE = 131;
    public static final float MINING_SPEED = 4.0F;
    public static final int ENCHANTMENT_VALUE = 5;
    public static final int MINING_TIER = 1;

    public static final float ATTACK_DAMAGE_BASELINE = 7.0F;
    public static final float ATTACK_SPEED_BASELINE = -3.2F;

    public static final boolean FIRE_RESISTANT = false;

    private StoneAxe() {
    }

    public static float baseDestroySpeed(String blockName, ToolEra era) {
        return Axe.baseDestroySpeed(MATERIAL, blockName, era);
    }

    public static boolean correctForDrops(boolean requiresCorrectToolForDrops, String blockName, ToolEra era) {
        return Axe.correctForDrops(MATERIAL, requiresCorrectToolForDrops, blockName, era);
    }

    public static double attackDamage() {
        return Axe.attackDamage(MATERIAL, ATTACK_DAMAGE_BASELINE);
    }

    public static double attackSpeed() {
        return Axe.attackSpeed(ATTACK_SPEED_BASELINE);
    }

    public static double attackCooldownTicks() {
        return Axe.attackCooldownTicks(ATTACK_SPEED_BASELINE);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

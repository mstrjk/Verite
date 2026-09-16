package teacommontea.veritechasse.Vanilla.Tools.Axes;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Axes.Support.Axe;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class IronAxe {

    public static final String KEY = "iron_axe";
    public static final Material MATERIAL = Materials.IRON;

    public static final int MAX_DAMAGE = 250;
    public static final float MINING_SPEED = 6.0F;
    public static final int ENCHANTMENT_VALUE = 14;
    public static final int MINING_TIER = 2;

    public static final float ATTACK_DAMAGE_BASELINE = 6.0F;
    public static final float ATTACK_SPEED_BASELINE = -3.1F;

    public static final boolean FIRE_RESISTANT = false;

    private IronAxe() {
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

package teacommontea.veritechasse.vanilla.Tools.Axes;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Axes.Support.Axe;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class NetheriteAxe {

    public static final String KEY = "netherite_axe";
    public static final Material MATERIAL = Materials.NETHERITE;

    public static final int MAX_DAMAGE = 2031;
    public static final float MINING_SPEED = 9.0F;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final int MINING_TIER = 4;

    public static final float ATTACK_DAMAGE_BASELINE = 5.0F;
    public static final float ATTACK_SPEED_BASELINE = -3.0F;

    public static final boolean FIRE_RESISTANT = true;

    private NetheriteAxe() {
    }

    public static boolean survivesLava() {
        return FIRE_RESISTANT;
    }

    public static boolean survivesFire() {
        return FIRE_RESISTANT;
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

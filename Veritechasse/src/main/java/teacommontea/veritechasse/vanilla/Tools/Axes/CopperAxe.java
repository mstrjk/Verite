package teacommontea.veritechasse.vanilla.Tools.Axes;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Axes.Support.Axe;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class CopperAxe {

    public static final String KEY = "copper_axe";
    public static final Material MATERIAL = Materials.COPPER;

    public static final int MAX_DAMAGE = 190;
    public static final float MINING_SPEED = 5.0F;
    public static final int ENCHANTMENT_VALUE = 13;
    public static final int MINING_TIER = 1;

    public static final float ATTACK_DAMAGE_BASELINE = 7.0F;
    public static final float ATTACK_SPEED_BASELINE = -3.2F;

    public static final boolean FIRE_RESISTANT = false;

    private CopperAxe() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 21, 9);
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

package teacommontea.veritechasse.vanilla.Tools.Hoes;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Hoes.Support.Hoe;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class NetheriteHoe {

    public static final String KEY = "netherite_hoe";
    public static final Material MATERIAL = Materials.NETHERITE;

    public static final int MAX_DAMAGE = 2031;
    public static final float MINING_SPEED = 9.0F;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final int MINING_TIER = 4;

    public static final float ATTACK_DAMAGE_BASELINE = -4.0F;
    public static final float ATTACK_SPEED_BASELINE = 0.0F;

    public static final boolean FIRE_RESISTANT = true;

    private NetheriteHoe() {
    }

    public static boolean survivesLava() {
        return FIRE_RESISTANT;
    }

    public static boolean survivesFire() {
        return FIRE_RESISTANT;
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

package teacommontea.veritechasse.Vanilla.Tools.Shovels;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Shovels.Support.Shovel;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class NetheriteShovel {

    public static final String KEY = "netherite_shovel";
    public static final Material MATERIAL = Materials.NETHERITE;

    public static final int MAX_DAMAGE = 2031;
    public static final float MINING_SPEED = 9.0F;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final int MINING_TIER = 4;

    public static final boolean FIRE_RESISTANT = true;

    private NetheriteShovel() {
    }

    public static boolean survivesLava() {
        return FIRE_RESISTANT;
    }

    public static boolean survivesFire() {
        return FIRE_RESISTANT;
    }

    public static float baseDestroySpeed(String blockName, ToolEra era) {
        return Shovel.baseDestroySpeed(MATERIAL, blockName, era);
    }

    public static boolean correctForDrops(boolean requiresCorrectToolForDrops, String blockName, ToolEra era) {
        return Shovel.correctForDrops(MATERIAL, requiresCorrectToolForDrops, blockName, era);
    }

    public static double attackDamage() {
        return Shovel.attackDamage(MATERIAL);
    }

    public static double attackSpeed() {
        return Shovel.attackSpeed();
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

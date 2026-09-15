package teacommontea.veritechasse.vanilla.Tools.Shovels.Support;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Support.CorrectForDrops;
import teacommontea.veritechasse.vanilla.Tools.Support.DestroySpeed;
import teacommontea.veritechasse.vanilla.Tools.Support.Durability;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.PlayerBase;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolKind;
import teacommontea.veritechasse.vanilla.Tools.Tags.MineableShovel;

public final class Shovel {

    public static final ToolKind KIND = ToolKind.SHOVEL;

    public static final float ATTACK_DAMAGE_BASELINE = 1.5F;
    public static final float ATTACK_SPEED_BASELINE = -3.0F;
    public static final float DISABLE_BLOCKING_SECONDS = 0.0F;

    public static final float INEFFECTIVE_SPEED = 1.0F;

    public static final int DAMAGE_PER_BLOCK = 1;
    public static final int DAMAGE_PER_ATTACK = 2;
    public static final int DAMAGE_PER_INTERACTION = 1;

    private Shovel() {
    }

    public static boolean minesEfficiently(String blockName, ToolEra era) {
        return MineableShovel.contains(blockName, era);
    }

    public static float baseDestroySpeed(Material material, String blockName, ToolEra era) {
        return minesEfficiently(blockName, era) ? material.speed() : INEFFECTIVE_SPEED;
    }

    public static boolean correctForDrops(Material material, boolean requiresCorrectToolForDrops, String blockName, ToolEra era) {
        return CorrectForDrops.resolve(requiresCorrectToolForDrops, KIND, material, blockName, era);
    }

    public static double attackDamage(Material material) {
        return PlayerBase.ATTACK_DAMAGE + (double) ATTACK_DAMAGE_BASELINE + (double) material.attackDamageBonus();
    }

    public static double attackSpeed() {
        return PlayerBase.ATTACK_SPEED + (double) ATTACK_SPEED_BASELINE;
    }

    public static double attackCooldownTicks() {
        return 20.0D / attackSpeed();
    }

    public static boolean flattens(String blockName, boolean airAbove, boolean clickedBottomFace) {
        return Flattenables.flattens(blockName, airAbove, clickedBottomFace);
    }

    public static boolean dowsesCampfire(boolean campfireLit, boolean clickedBottomFace) {
        return !clickedBottomFace && campfireLit;
    }

    public static int maxDamage(Material material) {
        return Durability.maxDamage(material);
    }

    public static float destroySpeed(
        Material material,
        String blockName,
        float miningEfficiency,
        int hasteAmplifier,
        int conduitPowerAmplifier,
        int miningFatigueAmplifier,
        float blockBreakSpeed,
        boolean eyeInWater,
        float submergedMiningSpeed,
        boolean aquaAffinity,
        boolean onGround,
        ToolEra era
    ) {
        return DestroySpeed.resolve(
            baseDestroySpeed(material, blockName, era),
            miningEfficiency,
            hasteAmplifier,
            conduitPowerAmplifier,
            miningFatigueAmplifier,
            blockBreakSpeed,
            eyeInWater,
            submergedMiningSpeed,
            aquaAffinity,
            onGround,
            era);
    }

    public static Reality destroySpeedFrom(float speed) {
        return Reality.of(speed);
    }
}

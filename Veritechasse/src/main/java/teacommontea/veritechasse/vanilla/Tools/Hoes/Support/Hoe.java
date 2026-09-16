package teacommontea.veritechasse.Vanilla.Tools.Hoes.Support;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Support.CorrectForDrops;
import teacommontea.veritechasse.Vanilla.Tools.Support.DestroySpeed;
import teacommontea.veritechasse.Vanilla.Tools.Support.Durability;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.PlayerBase;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolKind;
import teacommontea.veritechasse.Vanilla.Tools.Tags.MineableHoe;

public final class Hoe {

    public static final ToolKind KIND = ToolKind.HOE;

    public static final float DISABLE_BLOCKING_SECONDS = 0.0F;
    public static final float INEFFECTIVE_SPEED = 1.0F;

    public static final int DAMAGE_PER_BLOCK = 1;
    public static final int DAMAGE_PER_ATTACK = 2;
    public static final int DAMAGE_PER_INTERACTION = 1;

    public static final double UNIFORM_ATTACK_DAMAGE = 1.0D;

    private Hoe() {
    }

    public static boolean minesEfficiently(String blockName, ToolEra era) {
        return MineableHoe.contains(blockName, era);
    }

    public static float baseDestroySpeed(Material material, String blockName, ToolEra era) {
        return minesEfficiently(blockName, era) ? material.speed() : INEFFECTIVE_SPEED;
    }

    public static boolean correctForDrops(Material material, boolean requiresCorrectToolForDrops, String blockName, ToolEra era) {
        return CorrectForDrops.resolve(requiresCorrectToolForDrops, KIND, material, blockName, era);
    }

    public static double attackDamage(Material material, float attackDamageBaseline) {
        return PlayerBase.ATTACK_DAMAGE + (double) attackDamageBaseline + (double) material.attackDamageBonus();
    }

    public static double attackSpeed(float attackSpeedBaseline) {
        return PlayerBase.ATTACK_SPEED + (double) attackSpeedBaseline;
    }

    public static double attackCooldownTicks(float attackSpeedBaseline) {
        return 20.0D / attackSpeed(attackSpeedBaseline);
    }

    public static boolean tills(String blockName, boolean airAbove, boolean clickedBottomFace) {
        return Tillables.tills(blockName, airAbove, clickedBottomFace);
    }

    public static String tilledForm(String blockName) {
        return Tillables.tilledForm(blockName);
    }

    public static String droppedItem(String blockName) {
        return Tillables.droppedItem(blockName);
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

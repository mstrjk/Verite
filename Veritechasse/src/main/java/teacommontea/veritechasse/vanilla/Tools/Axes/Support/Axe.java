package teacommontea.veritechasse.vanilla.Tools.Axes.Support;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Support.CorrectForDrops;
import teacommontea.veritechasse.vanilla.Tools.Support.DestroySpeed;
import teacommontea.veritechasse.vanilla.Tools.Support.Durability;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.PlayerBase;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolKind;
import teacommontea.veritechasse.vanilla.Tools.Tags.MineableAxe;

public final class Axe {

    public static final ToolKind KIND = ToolKind.AXE;

    public static final float DISABLE_BLOCKING_SECONDS = 5.0F;
    public static final float INEFFECTIVE_SPEED = 1.0F;

    public static final int DAMAGE_PER_BLOCK = 1;
    public static final int DAMAGE_PER_ATTACK = 2;
    public static final int DAMAGE_PER_INTERACTION = 1;

    private Axe() {
    }

    public static boolean minesEfficiently(String blockName, ToolEra era) {
        return MineableAxe.contains(blockName, era);
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

    public static boolean disablesBlocking() {
        return true;
    }

    public static int shieldCooldownTicks() {
        return ShieldDisable.cooldownTicks();
    }

    public static boolean strips(String blockName, Protocol protocol) {
        return Strippables.strippable(blockName, protocol);
    }

    public static String strippedForm(String blockName, Protocol protocol) {
        return Strippables.strippedForm(blockName, protocol);
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

package teacommontea.veritechasse.Vanilla.Tools.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.MiningModifiers;
import teacommontea.veritechasse.Vanilla.Potions.Support.DigSpeed;
import teacommontea.veritechasse.Vanilla.Reality;

public final class DestroySpeed {

    public static final float AIRBORNE_DIVISOR = 5.0F;
    public static final float LEGACY_SUBMERGED_DIVISOR = 5.0F;
    public static final float EFFICIENCY_THRESHOLD = 1.0F;

    public static final float DEFAULT_BLOCK_BREAK_SPEED = 1.0F;
    public static final float DEFAULT_SUBMERGED_MINING_SPEED = 0.2F;

    private DestroySpeed() {
    }

    public static float withEfficiency(float baseSpeed, float miningEfficiency) {
        if (baseSpeed <= EFFICIENCY_THRESHOLD) {
            return baseSpeed;
        }
        return baseSpeed + miningEfficiency;
    }

    public static float legacyEfficiencyBonus(int efficiencyLevel) {
        if (efficiencyLevel <= 0) {
            return 0.0F;
        }
        return (float) (efficiencyLevel * efficiencyLevel + 1);
    }

    public static float applySubmerged(float speed, float submergedMiningSpeed, ToolEra era, boolean aquaAffinity) {
        if (era == ToolEra.MATERIALS_WITH_TAGS) {
            return speed * submergedMiningSpeed;
        }
        return aquaAffinity ? speed : speed / LEGACY_SUBMERGED_DIVISOR;
    }

    public static float applyAirborne(float speed) {
        return speed / AIRBORNE_DIVISOR;
    }

    public static float resolve(
        float baseSpeed,
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
        float speed = withEfficiency(baseSpeed, miningEfficiency);
        speed = DigSpeed.apply(speed, hasteAmplifier, conduitPowerAmplifier, miningFatigueAmplifier);

        if (era == ToolEra.MATERIALS_WITH_TAGS) {
            speed = speed * blockBreakSpeed;
        }

        if (eyeInWater) {
            speed = applySubmerged(speed, submergedMiningSpeed, era, aquaAffinity);
        }

        if (!onGround) {
            speed = applyAirborne(speed);
        }

        return speed;
    }

    public static float resolveFor(
        float baseSpeed,
        ItemStack tool,
        ItemStack helmet,
        int hasteAmplifier,
        int conduitPowerAmplifier,
        int miningFatigueAmplifier,
        float blockBreakSpeed,
        boolean eyeInWater,
        boolean onGround,
        ToolEra toolEra,
        Era enchantEra
    ) {
        float miningEfficiency = MiningModifiers.miningEfficiency(tool, enchantEra);
        float submerged = MiningModifiers.submergedMiningSpeed(helmet, enchantEra);
        boolean aquaAffinity = MiningModifiers.hasAquaAffinity(helmet);

        if (toolEra == ToolEra.TIERS_WITH_LEVELS) {
            miningEfficiency = legacyEfficiencyBonus(MiningModifiers.efficiencyLevel(tool));
        }

        return resolve(
            baseSpeed,
            miningEfficiency,
            hasteAmplifier,
            conduitPowerAmplifier,
            miningFatigueAmplifier,
            blockBreakSpeed,
            eyeInWater,
            submerged,
            aquaAffinity,
            onGround,
            toolEra);
    }

    public static Reality from(float speed) {
        return Reality.of(speed);
    }
}

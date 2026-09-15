package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerVictim;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Thorns;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Shields.Support.BlocksAttacks;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.vanilla.Protocol;

public final class ExchangeReality {

    public static final float TOLERANCE = 1.0E-4F;

    private ExchangeReality() {
    }

    public static float dealtToVictim(
            float attackerOutgoingDamage,
            boolean blocking,
            double blockingAngleRadians,
            boolean blockBypassed,
            float totalArmour,
            float armourToughness,
            float totalProtection,
            ActiveEffects victimEffects,
            String damageType,
            EffectEra effectEra,
            boolean damagesHelmet,
            boolean wearingHelmet,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        float afterShield = attackerOutgoingDamage;
        if (blocking) {
            afterShield = BlocksAttacks.damageAfterBlocking(
                attackerOutgoingDamage, blockingAngleRadians, blockBypassed);
        }
        return VictimReality.expectedDamage(
            afterShield, totalArmour, armourToughness, totalProtection,
            victimEffects, damageType, effectEra,
            damagesHelmet, wearingHelmet,
            lastHurt, invulnerableTime, bypassesCooldown, protocol);
    }

    public static float maximumThornsToAttacker(ItemStack victimChest, Era era) {
        int level = Thorns.levelOn(victimChest);
        if (level <= 0) {
            return 0.0F;
        }
        return Thorns.maxReflectedDamage(level, era);
    }

    public static boolean thornsIsPossible(ItemStack victimChest) {
        return Thorns.levelOn(victimChest) > 0;
    }

    public static float minimumThornsToAttacker(ItemStack victimChest, Era era) {
        int level = Thorns.levelOn(victimChest);
        if (level <= 0) {
            return 0.0F;
        }
        return Thorns.minReflectedDamage(level, era);
    }

    public static boolean attackerLostUnexplainedHealth(
            float attackerHealthLost,
            ItemStack victimChest,
            Era era) {
        if (attackerHealthLost <= TOLERANCE) {
            return false;
        }
        return attackerHealthLost > maximumThornsToAttacker(victimChest, era) + TOLERANCE;
    }

    public static boolean thornsDamageIsImpossible(
            float observedAttackerHealthLost,
            ItemStack victimChest,
            Era era) {
        if (observedAttackerHealthLost <= TOLERANCE) {
            return false;
        }
        return !Thorns.reflectedDamageIsPossible(
            observedAttackerHealthLost, Thorns.levelOn(victimChest), era);
    }

    public static boolean victimDamageDisagreesWithAttacker(
            float observedVictimHealthLost,
            float expectedFromAttacker) {
        return VictimReality.damageDisagrees(
            observedVictimHealthLost, expectedFromAttacker);
    }

    public static boolean blockedWithoutShieldReady(
            boolean claimedBlocking,
            int ticksHeld,
            Protocol protocol) {
        if (!claimedBlocking) {
            return false;
        }
        return !BlocksAttacks.delayElapsed(ticksHeld);
    }

    public static boolean blockedOutsideAngle(
            boolean claimedBlocking,
            double angleRadians) {
        if (!claimedBlocking) {
            return false;
        }
        return !BlocksAttacks.withinBlockingAngle(angleRadians);
    }
}

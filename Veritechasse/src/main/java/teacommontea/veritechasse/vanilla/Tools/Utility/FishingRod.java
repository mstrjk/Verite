package teacommontea.veritechasse.vanilla.Tools.Utility;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.LuckOfTheSea;
import teacommontea.veritechasse.vanilla.Enchantments.Lure;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Utility.Support.BobberCast;

public final class FishingRod {

    public static final String KEY = "fishing_rod";

    public static final int MAX_DAMAGE = 64;
    public static final int ENCHANTMENT_VALUE = 1;

    public static final int DAMAGE_RETRIEVE_NOTHING = 0;
    public static final int DAMAGE_RETRIEVE_ITEM_ENTITY = 3;
    public static final int DAMAGE_RETRIEVE_MOB = 5;
    public static final int DAMAGE_RETRIEVE_CATCH = 1;

    public static final boolean ENCHANTABLE = true;
    public static final boolean FIRE_RESISTANT = false;

    private FishingRod() {
    }

    public static int retrieveDamage(boolean hookedEntity, boolean hookedIsItemEntity, boolean hadNibble) {
        if (hookedEntity) {
            return hookedIsItemEntity ? DAMAGE_RETRIEVE_ITEM_ENTITY : DAMAGE_RETRIEVE_MOB;
        }
        if (hadNibble) {
            return DAMAGE_RETRIEVE_CATCH;
        }
        return DAMAGE_RETRIEVE_NOTHING;
    }

    public static double maxCastDistance() {
        return BobberCast.MAX_DISTANCE;
    }

    public static boolean castPossible(double observedDistance) {
        return observedDistance <= BobberCast.MAX_DISTANCE;
    }

    public static Reality castDistanceFrom(double observedDistance) {
        return BobberCast.distanceFrom(observedDistance);
    }

    public static int lureSpeedTicks(float fishingTimeReductionSeconds) {
        return (int) (fishingTimeReductionSeconds * 20.0F);
    }

    public static float totalLuck(int rodLuckBonus, float playerLuck) {
        return (float) rodLuckBonus + playerLuck;
    }

    public static int resolvedLureSpeedTicks(ItemStack rod, Era era) {
        if (rod == null) {
            return 0;
        }
        return lureSpeedTicks(Lure.timeReductionSeconds(Lure.levelOn(rod), era));
    }

    public static float resolvedLuck(ItemStack rod, float playerLuck, Era era) {
        if (rod == null) {
            return playerLuck;
        }
        return LuckOfTheSea.fishingLuckBonus(LuckOfTheSea.levelOn(rod), era) + playerLuck;
    }

    public static int resolvedMinWaitTicks(ItemStack rod, Era era) {
        if (rod == null) {
            return Lure.minWaitTicks(0, era);
        }
        return Lure.minWaitTicks(Lure.levelOn(rod), era);
    }

    public static int resolvedMaxWaitTicks(ItemStack rod, Era era) {
        if (rod == null) {
            return Lure.maxWaitTicks(0, era);
        }
        return Lure.maxWaitTicks(Lure.levelOn(rod), era);
    }

    public static int remaining(int damage) {
        int left = MAX_DAMAGE - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(int damage) {
        return damage >= MAX_DAMAGE;
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(remaining(damage));
    }
}

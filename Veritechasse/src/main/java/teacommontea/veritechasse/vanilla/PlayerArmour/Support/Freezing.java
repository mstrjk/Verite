package teacommontea.veritechasse.Vanilla.PlayerArmour.Support;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.PlayerArmour.Tags.FreezeHurtsExtraTypes;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Tags.FreezeImmuneWearables;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Freezing {

    public static final int BASE_TICKS_REQUIRED_TO_FREEZE = 140;
    public static final int FREEZE_HURT_FREQUENCY = 40;

    public static final int TICKS_GAINED_PER_TICK_IN_SNOW = 1;
    public static final int TICKS_LOST_PER_TICK_OUT_OF_SNOW = 2;

    public static final float FREEZE_DAMAGE = 1.0F;
    public static final float EXTRA_TYPE_MULTIPLIER = 5.0F;

    public static final int FLAT_DAMAGE_PROTOCOL_MAJOR = 1;
    public static final int FLAT_DAMAGE_PROTOCOL_MINOR = 19;
    public static final int FLAT_DAMAGE_PROTOCOL_PATCH = 4;

    private Freezing() {
    }

    public static boolean immuneWearable(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return FreezeImmuneWearables.contains(stack.getType().name().toLowerCase(Locale.ROOT));
    }

    public static boolean canFreeze(
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots) {
        if (immuneWearable(helmet)) {
            return false;
        }
        if (immuneWearable(chestplate)) {
            return false;
        }
        if (immuneWearable(leggings)) {
            return false;
        }
        return !immuneWearable(boots);
    }

    public static int ticksRequiredToFreeze() {
        return BASE_TICKS_REQUIRED_TO_FREEZE;
    }

    public static boolean fullyFrozen(int ticksFrozen) {
        return ticksFrozen >= BASE_TICKS_REQUIRED_TO_FREEZE;
    }

    public static float percentFrozen(int ticksFrozen) {
        int clamped = ticksFrozen < 0 ? 0 : ticksFrozen;
        if (clamped > BASE_TICKS_REQUIRED_TO_FREEZE) {
            clamped = BASE_TICKS_REQUIRED_TO_FREEZE;
        }
        return (float) clamped / (float) BASE_TICKS_REQUIRED_TO_FREEZE;
    }

    public static int ticksAfter(int ticksFrozen, boolean inPowderSnow, boolean canFreeze) {
        if (inPowderSnow && canFreeze) {
            int gained = ticksFrozen + TICKS_GAINED_PER_TICK_IN_SNOW;
            return gained > BASE_TICKS_REQUIRED_TO_FREEZE ? BASE_TICKS_REQUIRED_TO_FREEZE : gained;
        }
        int lost = ticksFrozen - TICKS_LOST_PER_TICK_OUT_OF_SNOW;
        return lost < 0 ? 0 : lost;
    }

    public static boolean hurtsThisTick(int tickCount, int ticksFrozen, boolean canFreeze) {
        return tickCount % FREEZE_HURT_FREQUENCY == 0 && fullyFrozen(ticksFrozen) && canFreeze;
    }

    public static float damage(String entityName, Protocol protocol) {
        if (!FreezeHurtsExtraTypes.contains(entityName)) {
            return FREEZE_DAMAGE;
        }
        if (protocol.atLeast(FLAT_DAMAGE_PROTOCOL_MAJOR, FLAT_DAMAGE_PROTOCOL_MINOR, FLAT_DAMAGE_PROTOCOL_PATCH)) {
            return FREEZE_DAMAGE * EXTRA_TYPE_MULTIPLIER;
        }
        return EXTRA_TYPE_MULTIPLIER;
    }

    public static int ticksToFreezeFrom(int ticksFrozen) {
        int left = BASE_TICKS_REQUIRED_TO_FREEZE - ticksFrozen;
        return left < 0 ? 0 : left;
    }

    public static Reality freezeTicksFrom(int ticksFrozen) {
        return Reality.of(ticksFrozen);
    }

    public static Reality damageFrom(String entityName, Protocol protocol) {
        return Reality.of(damage(entityName, protocol));
    }
}

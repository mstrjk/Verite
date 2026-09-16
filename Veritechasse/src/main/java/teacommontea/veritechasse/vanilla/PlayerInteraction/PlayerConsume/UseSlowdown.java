package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerConsume;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Spears;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Shields.Shield;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Tools.Ranged.Bow;
import teacommontea.veritechasse.Vanilla.Tools.Ranged.Crossbow;
import teacommontea.veritechasse.Vanilla.Tools.Spears.Support.Spear;
import teacommontea.veritechasse.Vanilla.Tools.Trident.Trident;
import teacommontea.veritechasse.Vanilla.Tools.Utility.Brush;

public final class UseSlowdown {

    public static final String KEY = "use_slowdown";

    public static final float LEGACY_MULTIPLIER = 0.2F;

    public static final float DEFAULT_MULTIPLIER = 0.2F;
    public static final float NO_SLOWDOWN_MULTIPLIER = 1.0F;

    public static final float MINIMUM_MULTIPLIER = 0.0F;
    public static final float MAXIMUM_MULTIPLIER = 1.0F;

    public static final float INPUT_PRESCALE = 0.98F;

    public static final int COMPONENT_MAJOR = 1;
    public static final int COMPONENT_MINOR = 21;
    public static final int COMPONENT_PATCH = 11;

    private UseSlowdown() {
    }

    public static boolean isComponentDriven(Protocol protocol) {
        return protocol.atLeast(COMPONENT_MAJOR, COMPONENT_MINOR, COMPONENT_PATCH);
    }

    public static float multiplier(Protocol protocol) {
        return isComponentDriven(protocol) ? DEFAULT_MULTIPLIER : LEGACY_MULTIPLIER;
    }

    public static float multiplierFor(String itemName, Protocol protocol) {
        if (!isComponentDriven(protocol)) {
            return LEGACY_MULTIPLIER;
        }
        if (Spears.TAG.contains(itemName) && Spear.exists(protocol)) {
            return Spear.useSpeedMultiplier();
        }
        if (Bow.KEY.equals(itemName)) {
            return Bow.useSpeedMultiplier();
        }
        if (Crossbow.KEY.equals(itemName)) {
            return Crossbow.useSpeedMultiplier();
        }
        if (Trident.KEY.equals(itemName)) {
            return Trident.useSpeedMultiplier();
        }
        if (Shield.KEY.equals(itemName)) {
            return Shield.useSpeedMultiplier();
        }
        if (Brush.KEY.equals(itemName)) {
            return Brush.useSpeedMultiplier();
        }
        return DEFAULT_MULTIPLIER;
    }

    public static boolean permitsSprintingWith(String itemName, Protocol protocol) {
        if (!isComponentDriven(protocol)) {
            return false;
        }
        return Spears.TAG.contains(itemName)
            && Spear.exists(protocol)
            && Spear.permitsSprintingWhileUsed();
    }

    public static float multiplier(float declaredMultiplier, Protocol protocol) {
        if (!isComponentDriven(protocol)) {
            return LEGACY_MULTIPLIER;
        }
        if (declaredMultiplier < MINIMUM_MULTIPLIER) {
            return MINIMUM_MULTIPLIER;
        }
        return declaredMultiplier > MAXIMUM_MULTIPLIER
            ? MAXIMUM_MULTIPLIER
            : declaredMultiplier;
    }

    public static boolean appliesWhileRiding() {
        return false;
    }

    public static boolean applies(boolean usingItem, boolean riding) {
        return usingItem && !riding;
    }

    public static boolean permitsSprinting(Protocol protocol) {
        return isComponentDriven(protocol);
    }

    public static boolean permitsSprinting(boolean declaredCanSprint, Protocol protocol) {
        return isComponentDriven(protocol) && declaredCanSprint;
    }

    public static double terminalSpeed(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack boots,
            float percentFrozen,
            boolean onGround,
            Era era,
            float useMultiplier) {
        double unslowed = GroundSpeed.terminalSpeed(
            effects, sprinting, blockBelow, blockHere,
            boots, percentFrozen, onGround, false, era);
        return unslowed * (double) useMultiplier;
    }

    public static double terminalSpeed(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack boots,
            float percentFrozen,
            boolean onGround,
            Era era,
            Protocol protocol) {
        return terminalSpeed(
            effects, sprinting, blockBelow, blockHere,
            boots, percentFrozen, onGround, era, multiplier(protocol));
    }

    public static boolean movedTooFast(
            double observedHorizontal,
            double boundWhileUsing,
            double tolerance) {
        return observedHorizontal > boundWhileUsing + tolerance;
    }
}

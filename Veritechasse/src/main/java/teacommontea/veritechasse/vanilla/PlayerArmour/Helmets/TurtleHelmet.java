package teacommontea.veritechasse.vanilla.PlayerArmour.Helmets;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerArmour.Helmets.Support.Helmet;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Material;
import teacommontea.veritechasse.vanilla.PlayerArmour.Support.Materials;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class TurtleHelmet {

    public static final String KEY = "turtle_helmet";
    public static final Material MATERIAL = Materials.TURTLE_SCUTE;

    public static final int MAX_DAMAGE = 275;
    public static final int DEFENCE = 2;
    public static final int ENCHANTMENT_VALUE = 9;
    public static final float TOUGHNESS = 0.0F;
    public static final float KNOCKBACK_RESISTANCE = 0.0F;

    public static final boolean FIRE_RESISTANT = false;

    public static final int WATER_BREATHING_DURATION_TICKS = 200;
    public static final int WATER_BREATHING_AMPLIFIER = 0;
    public static final boolean WATER_BREATHING_AMBIENT = false;
    public static final boolean WATER_BREATHING_VISIBLE = false;
    public static final boolean WATER_BREATHING_SHOWS_ICON = true;

    public static final int COMPONENT_SLOT_PROTOCOL_MAJOR = 1;
    public static final int COMPONENT_SLOT_PROTOCOL_MINOR = 21;
    public static final int COMPONENT_SLOT_PROTOCOL_PATCH = 3;

    private TurtleHelmet() {
    }

    public static boolean is(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getType().name().toLowerCase(Locale.ROOT).equals(KEY);
    }

    public static boolean checksEveryEquipmentSlot(Protocol protocol) {
        return protocol.atLeast(
            COMPONENT_SLOT_PROTOCOL_MAJOR,
            COMPONENT_SLOT_PROTOCOL_MINOR,
            COMPONENT_SLOT_PROTOCOL_PATCH);
    }

    public static boolean grantsWaterBreathing(ItemStack head, boolean eyeInWater) {
        return !eyeInWater && is(head);
    }

    public static boolean refreshesThisTick(ItemStack head, boolean eyeInWater) {
        return grantsWaterBreathing(head, eyeInWater);
    }

    public static int waterBreathingTicksAfter(ItemStack head, boolean eyeInWater, int currentTicks) {
        if (grantsWaterBreathing(head, eyeInWater)) {
            return WATER_BREATHING_DURATION_TICKS;
        }
        int left = currentTicks - 1;
        return left < 0 ? 0 : left;
    }

    public static int defence() {
        return Helmet.defence(MATERIAL);
    }

    public static float toughness() {
        return Helmet.toughness(MATERIAL);
    }

    public static float knockbackResistance() {
        return Helmet.knockbackResistance(MATERIAL);
    }

    public static float protection(
            ItemStack stack,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        return Helmet.protection(stack, isFire, isExplosion, isProjectile, isFall, era);
    }

    public static Reality waterBreathingFrom() {
        return Reality.of(WATER_BREATHING_DURATION_TICKS);
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}

package teacommontea.veritechasse.vanilla.RideableArmour;

import java.util.Locale;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.PlayerArmour.Support.CombatRules;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.RideableArmour.Tags.CanWearHorseArmour;

public final class HorseArmour {

    public static final int MAX_STACK_SIZE = 1;

    public static final boolean HAS_DURABILITY = false;
    public static final boolean EQUIP_ON_INTERACT = false;
    public static final boolean CAN_BE_SHEARED = true;
    public static final boolean DAMAGE_ON_HURT = false;

    public static final String SLOT_NAME = "body";

    public static final String LEATHER_KEY = "leather_horse_armor";
    public static final String COPPER_KEY = "copper_horse_armor";
    public static final String IRON_KEY = "iron_horse_armor";
    public static final String GOLDEN_KEY = "golden_horse_armor";
    public static final String DIAMOND_KEY = "diamond_horse_armor";
    public static final String NETHERITE_KEY = "netherite_horse_armor";

    public static final int COPPER_MAJOR = 1;
    public static final int COPPER_MINOR = 21;
    public static final int COPPER_PATCH = 9;

    public static final Map<String, Integer> DEFENCE = Map.of(
        LEATHER_KEY, Integer.valueOf(3),
        COPPER_KEY, Integer.valueOf(4),
        IRON_KEY, Integer.valueOf(5),
        GOLDEN_KEY, Integer.valueOf(7),
        DIAMOND_KEY, Integer.valueOf(11),
        NETHERITE_KEY, Integer.valueOf(19));

    public static final Map<String, Float> TOUGHNESS = Map.of(
        LEATHER_KEY, Float.valueOf(0.0F),
        COPPER_KEY, Float.valueOf(0.0F),
        IRON_KEY, Float.valueOf(0.0F),
        GOLDEN_KEY, Float.valueOf(0.0F),
        DIAMOND_KEY, Float.valueOf(2.0F),
        NETHERITE_KEY, Float.valueOf(3.0F));

    public static final Map<String, Float> KNOCKBACK_RESISTANCE = Map.of(
        LEATHER_KEY, Float.valueOf(0.0F),
        COPPER_KEY, Float.valueOf(0.0F),
        IRON_KEY, Float.valueOf(0.0F),
        GOLDEN_KEY, Float.valueOf(0.0F),
        DIAMOND_KEY, Float.valueOf(0.0F),
        NETHERITE_KEY, Float.valueOf(0.1F));

    public static final float MAX_DEFENCE = 19.0F;
    public static final float MAX_TOUGHNESS = 3.0F;
    public static final float MAX_KNOCKBACK_RESISTANCE = 0.1F;

    private HorseArmour() {
    }

    public static boolean contains(String itemName) {
        return itemName != null
            && DEFENCE.containsKey(itemName.toLowerCase(Locale.ROOT));
    }

    public static boolean is(ItemStack stack) {
        return stack != null && contains(stack.getType().name());
    }

    public static boolean exists(String itemName, Protocol protocol) {
        if (!contains(itemName)) {
            return false;
        }
        if (COPPER_KEY.equals(itemName.toLowerCase(Locale.ROOT))) {
            return protocol.atLeast(COPPER_MAJOR, COPPER_MINOR, COPPER_PATCH);
        }
        return true;
    }

    public static int defenceOf(String itemName) {
        if (itemName == null) {
            return 0;
        }
        return DEFENCE.getOrDefault(itemName.toLowerCase(Locale.ROOT), Integer.valueOf(0)).intValue();
    }

    public static float toughnessOf(String itemName) {
        if (itemName == null) {
            return 0.0F;
        }
        return TOUGHNESS.getOrDefault(itemName.toLowerCase(Locale.ROOT), Float.valueOf(0.0F)).floatValue();
    }

    public static float knockbackResistanceOf(String itemName) {
        if (itemName == null) {
            return 0.0F;
        }
        return KNOCKBACK_RESISTANCE
            .getOrDefault(itemName.toLowerCase(Locale.ROOT), Float.valueOf(0.0F)).floatValue();
    }

    public static boolean canBeWornBy(String entityName, Protocol protocol) {
        return CanWearHorseArmour.contains(entityName, protocol);
    }

    public static boolean equipIsPossible(String itemName, String entityName, Protocol protocol) {
        return exists(itemName, protocol) && canBeWornBy(entityName, protocol);
    }

    public static float damageAfterArmour(float damage, String itemName) {
        return CombatRules.damageAfterAbsorb(
            damage, (float) defenceOf(itemName), toughnessOf(itemName));
    }

    public static float minimumDamageTaken(float damage, String entityName, Protocol protocol) {
        if (!canBeWornBy(entityName, protocol)) {
            return damage;
        }
        return CombatRules.damageAfterAbsorb(damage, MAX_DEFENCE, MAX_TOUGHNESS);
    }

    public static boolean damageIsPossible(
            float observedDamage,
            float incomingDamage,
            String entityName,
            Protocol protocol) {
        return observedDamage
            >= minimumDamageTaken(incomingDamage, entityName, protocol) - DAMAGE_TOLERANCE;
    }

    public static final float DAMAGE_TOLERANCE = 1.0E-4F;

    public static boolean takesDurabilityDamage() {
        return DAMAGE_ON_HURT;
    }

    public static Reality damageFrom(float damage, String itemName) {
        return Reality.of(damageAfterArmour(damage, itemName));
    }

    public static Reality minimumDamageFrom(float damage, String entityName, Protocol protocol) {
        return Reality.of(minimumDamageTaken(damage, entityName, protocol));
    }
}

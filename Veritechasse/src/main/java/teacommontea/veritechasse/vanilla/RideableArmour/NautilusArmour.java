package teacommontea.veritechasse.Vanilla.RideableArmour;

import java.util.Locale;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.CombatRules;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.RideableArmour.Tags.CanWearNautilusArmour;

public final class NautilusArmour {

    public static final int MAX_STACK_SIZE = 1;

    public static final boolean HAS_DURABILITY = false;
    public static final boolean EQUIP_ON_INTERACT = true;
    public static final boolean CAN_BE_SHEARED = true;
    public static final boolean DAMAGE_ON_HURT = false;

    public static final String SLOT_NAME = "body";

    public static final int PROTOCOL_MAJOR = 1;
    public static final int PROTOCOL_MINOR = 21;
    public static final int PROTOCOL_PATCH = 11;

    public static final String COPPER_KEY = "copper_nautilus_armor";
    public static final String IRON_KEY = "iron_nautilus_armor";
    public static final String GOLDEN_KEY = "golden_nautilus_armor";
    public static final String DIAMOND_KEY = "diamond_nautilus_armor";
    public static final String NETHERITE_KEY = "netherite_nautilus_armor";

    public static final Map<String, Integer> DEFENCE = Map.of(
        COPPER_KEY, 4,
        IRON_KEY, 5,
        GOLDEN_KEY, 7,
        DIAMOND_KEY, 11,
        NETHERITE_KEY, 19);

    public static final Map<String, Float> TOUGHNESS = Map.of(
        COPPER_KEY, 0.0F,
        IRON_KEY, 0.0F,
        GOLDEN_KEY, 0.0F,
        DIAMOND_KEY, 2.0F,
        NETHERITE_KEY, 3.0F);

    public static final Map<String, Float> KNOCKBACK_RESISTANCE = Map.of(
        COPPER_KEY, 0.0F,
        IRON_KEY, 0.0F,
        GOLDEN_KEY, 0.0F,
        DIAMOND_KEY, 0.0F,
        NETHERITE_KEY, 0.1F);

    public static final Map<String, Boolean> FIRE_RESISTANT = Map.of(
        COPPER_KEY, Boolean.FALSE,
        IRON_KEY, Boolean.FALSE,
        GOLDEN_KEY, Boolean.FALSE,
        DIAMOND_KEY, Boolean.FALSE,
        NETHERITE_KEY, Boolean.TRUE);

    private NautilusArmour() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(PROTOCOL_MAJOR, PROTOCOL_MINOR, PROTOCOL_PATCH);
    }

    public static boolean contains(String itemName) {
        return itemName != null
            && DEFENCE.containsKey(itemName.toLowerCase(Locale.ROOT));
    }

    public static boolean is(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return contains(stack.getType().name());
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

    public static boolean fireResistant(String itemName) {
        if (itemName == null) {
            return false;
        }
        return FIRE_RESISTANT
            .getOrDefault(itemName.toLowerCase(Locale.ROOT), Boolean.FALSE).booleanValue();
    }

    public static boolean canBeEquippedTo(String entityName, Protocol protocol) {
        return CanWearNautilusArmour.contains(entityName, protocol);
    }

    public static boolean equipIsPossible(String itemName, String entityName, Protocol protocol) {
        return exists(protocol)
            && contains(itemName)
            && canBeEquippedTo(entityName, protocol);
    }

    public static boolean takesDurabilityDamage() {
        return DAMAGE_ON_HURT;
    }

    public static final float MAX_DEFENCE = 19.0F;
    public static final float MAX_TOUGHNESS = 3.0F;
    public static final float DAMAGE_TOLERANCE = 1.0E-4F;

    public static float damageAfterArmour(float damage, String itemName) {
        return CombatRules.damageAfterAbsorb(
            damage, (float) defenceOf(itemName), toughnessOf(itemName));
    }

    public static float minimumDamageTaken(float damage, String entityName, Protocol protocol) {
        if (!canBeEquippedTo(entityName, protocol)) {
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

    public static Reality defenceFrom(String itemName) {
        return Reality.of(defenceOf(itemName));
    }

    public static Reality toughnessFrom(String itemName) {
        return Reality.of(toughnessOf(itemName));
    }

    public static Reality damageFrom(float damage, String itemName) {
        return Reality.of(damageAfterArmour(damage, itemName));
    }

    public static Reality minimumDamageFrom(float damage, String entityName, Protocol protocol) {
        return Reality.of(minimumDamageTaken(damage, entityName, protocol));
    }
}

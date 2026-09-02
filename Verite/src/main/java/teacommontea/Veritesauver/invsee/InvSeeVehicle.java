package teacommontea.veritesauver.invsee;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;


final class InvSeeVehicle {

    private InvSeeVehicle() {}

    private static final String[][] TABLE = {
        {"ALLAY", "ALLAY_SPAWN_EGG"},
        {"ARMADILLO", "ARMADILLO_SPAWN_EGG"},
        {"AXOLOTL", "AXOLOTL_SPAWN_EGG"},
        {"BAT", "BAT_SPAWN_EGG"},
        {"BEE", "BEE_SPAWN_EGG"},
        {"BLAZE", "BLAZE_SPAWN_EGG"},
        {"BOGGED", "BOGGED_SPAWN_EGG"},
        {"BREEZE", "BREEZE_SPAWN_EGG"},
        {"CAMEL", "CAMEL_SPAWN_EGG"},
        {"CAMEL_HUSK", "CAMEL_HUSK_SPAWN_EGG"},
        {"CAT", "CAT_SPAWN_EGG"},
        {"CAVE_SPIDER", "CAVE_SPIDER_SPAWN_EGG"},
        {"CHICKEN", "CHICKEN_SPAWN_EGG"},
        {"COD", "COD_SPAWN_EGG"},
        {"COPPER_GOLEM", "COPPER_GOLEM_SPAWN_EGG"},
        {"COW", "COW_SPAWN_EGG"},
        {"CREAKING", "CREAKING_SPAWN_EGG"},
        {"CREEPER", "CREEPER_SPAWN_EGG"},
        {"DOLPHIN", "DOLPHIN_SPAWN_EGG"},
        {"DONKEY", "DONKEY_SPAWN_EGG"},
        {"DROWNED", "DROWNED_SPAWN_EGG"},
        {"ELDER_GUARDIAN", "ELDER_GUARDIAN_SPAWN_EGG"},
        {"ENDER_DRAGON", "ENDER_DRAGON_SPAWN_EGG"},
        {"ENDERMAN", "ENDERMAN_SPAWN_EGG"},
        {"ENDERMITE", "ENDERMITE_SPAWN_EGG"},
        {"EVOKER", "EVOKER_SPAWN_EGG"},
        {"FOX", "FOX_SPAWN_EGG"},
        {"FROG", "FROG_SPAWN_EGG"},
        {"GHAST", "GHAST_SPAWN_EGG"},
        {"GIANT", "ZOMBIE_SPAWN_EGG"},
        {"GLOW_SQUID", "GLOW_SQUID_SPAWN_EGG"},
        {"GOAT", "GOAT_SPAWN_EGG"},
        {"GUARDIAN", "GUARDIAN_SPAWN_EGG"},
        {"HAPPY_GHAST", "HAPPY_GHAST_SPAWN_EGG"},
        {"HOGLIN", "HOGLIN_SPAWN_EGG"},
        {"HORSE", "HORSE_SPAWN_EGG"},
        {"HUSK", "HUSK_SPAWN_EGG"},
        {"ILLUSIONER", "EVOKER_SPAWN_EGG"},
        {"IRON_GOLEM", "IRON_GOLEM_SPAWN_EGG"},
        {"LLAMA", "LLAMA_SPAWN_EGG"},
        {"MAGMA_CUBE", "MAGMA_CUBE_SPAWN_EGG"},
        {"MOOSHROOM", "MOOSHROOM_SPAWN_EGG"},
        {"MULE", "MULE_SPAWN_EGG"},
        {"NAUTILUS", "NAUTILUS_SPAWN_EGG"},
        {"OCELOT", "OCELOT_SPAWN_EGG"},
        {"PANDA", "PANDA_SPAWN_EGG"},
        {"PARCHED", "PARCHED_SPAWN_EGG"},
        {"PARROT", "PARROT_SPAWN_EGG"},
        {"PHANTOM", "PHANTOM_SPAWN_EGG"},
        {"PIG", "PIG_SPAWN_EGG"},
        {"PIGLIN", "PIGLIN_SPAWN_EGG"},
        {"PIGLIN_BRUTE", "PIGLIN_BRUTE_SPAWN_EGG"},
        {"PILLAGER", "PILLAGER_SPAWN_EGG"},
        {"POLAR_BEAR", "POLAR_BEAR_SPAWN_EGG"},
        {"PUFFERFISH", "PUFFERFISH_SPAWN_EGG"},
        {"RABBIT", "RABBIT_SPAWN_EGG"},
        {"RAVAGER", "RAVAGER_SPAWN_EGG"},
        {"SALMON", "SALMON_SPAWN_EGG"},
        {"SHEEP", "SHEEP_SPAWN_EGG"},
        {"SHULKER", "SHULKER_SPAWN_EGG"},
        {"SILVERFISH", "SILVERFISH_SPAWN_EGG"},
        {"SKELETON", "SKELETON_SPAWN_EGG"},
        {"SKELETON_HORSE", "SKELETON_HORSE_SPAWN_EGG"},
        {"SLIME", "SLIME_SPAWN_EGG"},
        {"SNIFFER", "SNIFFER_SPAWN_EGG"},
        {"SNOW_GOLEM", "SNOW_GOLEM_SPAWN_EGG"},
        {"SPIDER", "SPIDER_SPAWN_EGG"},
        {"SQUID", "SQUID_SPAWN_EGG"},
        {"STRAY", "STRAY_SPAWN_EGG"},
        {"STRIDER", "STRIDER_SPAWN_EGG"},
        {"SULFUR_CUBE", "SULFUR_CUBE_SPAWN_EGG"},
        {"TADPOLE", "TADPOLE_SPAWN_EGG"},
        {"TRADER_LLAMA", "TRADER_LLAMA_SPAWN_EGG"},
        {"TROPICAL_FISH", "TROPICAL_FISH_SPAWN_EGG"},
        {"TURTLE", "TURTLE_SPAWN_EGG"},
        {"VEX", "VEX_SPAWN_EGG"},
        {"VILLAGER", "VILLAGER_SPAWN_EGG"},
        {"VINDICATOR", "VINDICATOR_SPAWN_EGG"},
        {"WANDERING_TRADER", "WANDERING_TRADER_SPAWN_EGG"},
        {"WARDEN", "WARDEN_SPAWN_EGG"},
        {"WITCH", "WITCH_SPAWN_EGG"},
        {"WITHER", "WITHER_SPAWN_EGG"},
        {"WITHER_SKELETON", "WITHER_SKELETON_SPAWN_EGG"},
        {"WOLF", "WOLF_SPAWN_EGG"},
        {"ZOGLIN", "ZOGLIN_SPAWN_EGG"},
        {"ZOMBIE", "ZOMBIE_SPAWN_EGG"},
        {"ZOMBIE_HORSE", "ZOMBIE_HORSE_SPAWN_EGG"},
        {"ZOMBIE_NAUTILUS", "ZOMBIE_NAUTILUS_SPAWN_EGG"},
        {"ZOMBIE_VILLAGER", "ZOMBIE_VILLAGER_SPAWN_EGG"},
        {"ZOMBIFIED_PIGLIN", "ZOMBIFIED_PIGLIN_SPAWN_EGG"},
        {"ACACIA_BOAT", "ACACIA_BOAT"},
        {"BAMBOO_RAFT", "BAMBOO_RAFT"},
        {"BIRCH_BOAT", "BIRCH_BOAT"},
        {"CHERRY_BOAT", "CHERRY_BOAT"},
        {"DARK_OAK_BOAT", "DARK_OAK_BOAT"},
        {"JUNGLE_BOAT", "JUNGLE_BOAT"},
        {"MANGROVE_BOAT", "MANGROVE_BOAT"},
        {"OAK_BOAT", "OAK_BOAT"},
        {"PALE_OAK_BOAT", "PALE_OAK_BOAT"},
        {"SPRUCE_BOAT", "SPRUCE_BOAT"},
        {"ACACIA_CHEST_BOAT", "ACACIA_CHEST_BOAT"},
        {"BAMBOO_CHEST_RAFT", "BAMBOO_CHEST_RAFT"},
        {"BIRCH_CHEST_BOAT", "BIRCH_CHEST_BOAT"},
        {"CHERRY_CHEST_BOAT", "CHERRY_CHEST_BOAT"},
        {"DARK_OAK_CHEST_BOAT", "DARK_OAK_CHEST_BOAT"},
        {"JUNGLE_CHEST_BOAT", "JUNGLE_CHEST_BOAT"},
        {"MANGROVE_CHEST_BOAT", "MANGROVE_CHEST_BOAT"},
        {"OAK_CHEST_BOAT", "OAK_CHEST_BOAT"},
        {"PALE_OAK_CHEST_BOAT", "PALE_OAK_CHEST_BOAT"},
        {"SPRUCE_CHEST_BOAT", "SPRUCE_CHEST_BOAT"},
        {"MINECART", "MINECART"},
        {"CHEST_MINECART", "CHEST_MINECART"},
        {"FURNACE_MINECART", "FURNACE_MINECART"},
        {"HOPPER_MINECART", "HOPPER_MINECART"},
        {"TNT_MINECART", "TNT_MINECART"},
        {"COMMAND_BLOCK_MINECART", "COMMAND_BLOCK_MINECART"},
        {"SPAWNER_MINECART", "MINECART"}
    };

    private static final Map<EntityType, Material> RIDEABLE = buildTable();

    private static Map<EntityType, Material> buildTable() {
        Map<EntityType, Material> map = new EnumMap<>(EntityType.class);
        for (String[] pair : TABLE) {
            EntityType type = entityType(pair[0]);
            Material material = Material.getMaterial(pair[1]);
            if (type != null && material != null) {
                map.put(type, material);
            }
        }
        return map;
    }

    private static EntityType entityType(String name) {
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static ItemStack displayFor(Entity ridden) {
        if (ridden == null) {
            return null;
        }
        Material material = RIDEABLE.get(ridden.getType());
        if (material == null) {
            material = legacyBoat(ridden);
        }
        return material == null ? null : new ItemStack(material);
    }

    private static Material legacyBoat(Entity ridden) {
        String typeName = ridden.getType() == null ? "" : ridden.getType().name();
        if (!typeName.equals("BOAT") && !typeName.equals("CHEST_BOAT")) {
            return null;
        }
        boolean chest = typeName.equals("CHEST_BOAT");
        String wood = boatWood(ridden);
        if (wood == null) {
            return null;
        }
        if (wood.equals("BAMBOO")) {
            return Material.getMaterial(chest ? "BAMBOO_CHEST_RAFT" : "BAMBOO_RAFT");
        }
        return Material.getMaterial(chest ? wood + "_CHEST_BOAT" : wood + "_BOAT");
    }

    private static String boatWood(Entity ridden) {
        try {
            java.lang.reflect.Method m = ridden.getClass().getMethod("getBoatType");
            Object type = m.invoke(ridden);
            return type instanceof Enum<?> e ? e.name() : null;
        } catch (Throwable t) {
            return null;
        }
    }
}

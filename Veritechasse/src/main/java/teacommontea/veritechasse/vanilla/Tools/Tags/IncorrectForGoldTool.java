package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class IncorrectForGoldTool {

    public static final String KEY = "incorrect_for_gold_tool";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of(
        "ancient_debris",
        "chiseled_copper",
        "copper_block",
        "copper_bulb",
        "copper_chest",
        "copper_grate",
        "copper_ore",
        "copper_trapdoor",
        "crafter",
        "crying_obsidian",
        "cut_copper",
        "cut_copper_slab",
        "cut_copper_stairs",
        "deepslate_copper_ore",
        "deepslate_diamond_ore",
        "deepslate_emerald_ore",
        "deepslate_gold_ore",
        "deepslate_iron_ore",
        "deepslate_lapis_ore",
        "deepslate_redstone_ore",
        "diamond_block",
        "diamond_ore",
        "emerald_block",
        "emerald_ore",
        "exposed_chiseled_copper",
        "exposed_copper",
        "exposed_copper_bulb",
        "exposed_copper_chest",
        "exposed_copper_grate",
        "exposed_copper_trapdoor",
        "exposed_cut_copper",
        "exposed_cut_copper_slab",
        "exposed_cut_copper_stairs",
        "exposed_lightning_rod",
        "gold_block",
        "gold_ore",
        "iron_block",
        "iron_ore",
        "lapis_block",
        "lapis_ore",
        "lightning_rod",
        "netherite_block",
        "obsidian",
        "oxidized_chiseled_copper",
        "oxidized_copper",
        "oxidized_copper_bulb",
        "oxidized_copper_chest",
        "oxidized_copper_grate",
        "oxidized_copper_trapdoor",
        "oxidized_cut_copper",
        "oxidized_cut_copper_slab",
        "oxidized_cut_copper_stairs",
        "oxidized_lightning_rod",
        "raw_copper_block",
        "raw_gold_block",
        "raw_iron_block",
        "redstone_ore",
        "respawn_anchor",
        "waxed_chiseled_copper",
        "waxed_copper_block",
        "waxed_copper_bulb",
        "waxed_copper_chest",
        "waxed_copper_grate",
        "waxed_copper_trapdoor",
        "waxed_cut_copper",
        "waxed_cut_copper_slab",
        "waxed_cut_copper_stairs",
        "waxed_exposed_chiseled_copper",
        "waxed_exposed_copper",
        "waxed_exposed_copper_bulb",
        "waxed_exposed_copper_chest",
        "waxed_exposed_copper_grate",
        "waxed_exposed_copper_trapdoor",
        "waxed_exposed_cut_copper",
        "waxed_exposed_cut_copper_slab",
        "waxed_exposed_cut_copper_stairs",
        "waxed_exposed_lightning_rod",
        "waxed_lightning_rod",
        "waxed_oxidized_chiseled_copper",
        "waxed_oxidized_copper",
        "waxed_oxidized_copper_bulb",
        "waxed_oxidized_copper_chest",
        "waxed_oxidized_copper_grate",
        "waxed_oxidized_copper_trapdoor",
        "waxed_oxidized_cut_copper",
        "waxed_oxidized_cut_copper_slab",
        "waxed_oxidized_cut_copper_stairs",
        "waxed_oxidized_lightning_rod",
        "waxed_weathered_chiseled_copper",
        "waxed_weathered_copper",
        "waxed_weathered_copper_bulb",
        "waxed_weathered_copper_chest",
        "waxed_weathered_copper_grate",
        "waxed_weathered_copper_trapdoor",
        "waxed_weathered_cut_copper",
        "waxed_weathered_cut_copper_slab",
        "waxed_weathered_cut_copper_stairs",
        "waxed_weathered_lightning_rod",
        "weathered_chiseled_copper",
        "weathered_copper",
        "weathered_copper_bulb",
        "weathered_copper_chest",
        "weathered_copper_grate",
        "weathered_copper_trapdoor",
        "weathered_cut_copper",
        "weathered_cut_copper_slab",
        "weathered_cut_copper_stairs",
        "weathered_lightning_rod");

    private IncorrectForGoldTool() {
    }

    public static boolean contains(String blockName, ToolEra era) {
        if (blockName == null) {
            return false;
        }
        if (era != ToolEra.MATERIALS_WITH_TAGS) {
            throw new IllegalStateException(
                KEY + " does not exist before 1.21.3; use MiningTier levels instead");
        }
        return TAG.contains(blockName.toLowerCase(Locale.ROOT));
    }
}

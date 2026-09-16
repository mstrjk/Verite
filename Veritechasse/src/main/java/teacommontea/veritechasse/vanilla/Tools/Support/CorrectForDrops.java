package teacommontea.veritechasse.Vanilla.Tools.Support;

import teacommontea.veritechasse.Vanilla.Tools.Tags.NeedsDiamondTool;
import teacommontea.veritechasse.Vanilla.Tools.Tags.NeedsIronTool;
import teacommontea.veritechasse.Vanilla.Tools.Tags.NeedsStoneTool;

public final class CorrectForDrops {

    private CorrectForDrops() {
    }

    public static int requiredTier(String blockName, ToolEra era) {
        if (NeedsDiamondTool.contains(blockName, era)) {
            return MiningTier.DIAMOND;
        }
        if (NeedsIronTool.contains(blockName, era)) {
            return MiningTier.IRON;
        }
        if (NeedsStoneTool.contains(blockName, era)) {
            return MiningTier.STONE;
        }
        return MiningTier.NONE;
    }

    public static boolean tierPermits(Material material, String blockName, ToolEra era) {
        if (material == null) {
            return false;
        }
        return material.legacyMiningLevel() >= requiredTier(blockName, era);
    }

    public static boolean resolve(
        boolean requiresCorrectToolForDrops,
        ToolKind kind,
        Material material,
        String blockName,
        ToolEra era
    ) {
        if (!requiresCorrectToolForDrops) {
            return true;
        }
        if (!kind.hasToolComponent()) {
            return false;
        }
        if (!tierPermits(material, blockName, era)) {
            return false;
        }
        return kind.minesEfficiently(blockName, era);
    }
}

package teacommontea.veritechasse.Vanilla.Tools.Support;

import teacommontea.veritechasse.Vanilla.Tools.Tags.MineableAxe;
import teacommontea.veritechasse.Vanilla.Tools.Tags.MineableHoe;
import teacommontea.veritechasse.Vanilla.Tools.Tags.MineablePickaxe;
import teacommontea.veritechasse.Vanilla.Tools.Tags.MineableShovel;

public enum ToolKind {

    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    SWORD,
    SHEARS,
    OTHER,
    NONE;

    public boolean minesEfficiently(String blockName, ToolEra era) {
        switch (this) {
            case PICKAXE:
                return MineablePickaxe.contains(blockName, era);
            case AXE:
                return MineableAxe.contains(blockName, era);
            case SHOVEL:
                return MineableShovel.contains(blockName, era);
            case HOE:
                return MineableHoe.contains(blockName, era);
            default:
                return false;
        }
    }

    public boolean hasToolComponent() {
        return this != NONE;
    }
}

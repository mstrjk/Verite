package teacommontea.veritechasse.Vanilla.Tools.Support;

import teacommontea.veritechasse.Vanilla.Protocol;

public enum ToolEra {

    TIERS_WITH_LEVELS,
    MATERIALS_WITH_TAGS;

    public static ToolEra of(Protocol protocol) {
        return protocol.atLeast(1, 20, 5) ? MATERIALS_WITH_TAGS : TIERS_WITH_LEVELS;
    }
}

package teacommontea.veritechasse.Vanilla.Potions.Support;

import teacommontea.veritechasse.Vanilla.Protocol;

public enum EffectEra {

    EFFECTS_WITH_OVERRIDES,
    EFFECTS_AS_MODIFIERS;

    public static EffectEra of(Protocol protocol) {
        return protocol.atLeast(1, 20, 2) ? EFFECTS_AS_MODIFIERS : EFFECTS_WITH_OVERRIDES;
    }
}

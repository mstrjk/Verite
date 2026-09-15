package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;

public final class Nausea {

    public static final String KEY = "nausea";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;
    public static final boolean AFFECTS_SERVER_REALITY = false;

    public static final int BLEND_IN_TICKS = 150;
    public static final int BLEND_OUT_TICKS = 20;
    public static final int BLEND_OUT_ADVANCE_TICKS = 60;

    public static final int LEGACY_PADDING_DURATION_TICKS = 0;

    private Nausea() {
    }

    public static boolean hasBlendDuration(Protocol protocol) {
        if (protocol.atLeast(26, 1, 0)) {
            return true;
        }
        if (protocol.atLeast(1, 21, 5)) {
            return false;
        }
        return protocol.atLeast(1, 20, 5);
    }

    public static int blendInTicks(Protocol protocol) {
        return hasBlendDuration(protocol) ? BLEND_IN_TICKS : LEGACY_PADDING_DURATION_TICKS;
    }

    public static int blendOutTicks(Protocol protocol) {
        return hasBlendDuration(protocol) ? BLEND_OUT_TICKS : LEGACY_PADDING_DURATION_TICKS;
    }

    public static int blendOutAdvanceTicks(Protocol protocol) {
        return hasBlendDuration(protocol) ? BLEND_OUT_ADVANCE_TICKS : LEGACY_PADDING_DURATION_TICKS;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}

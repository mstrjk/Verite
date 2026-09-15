package teacommontea.veritechasse.vanilla.Tools.Support;

public final class MiningTier {

    public static final int NONE = 0;
    public static final int STONE = 1;
    public static final int IRON = 2;
    public static final int DIAMOND = 3;
    public static final int NETHERITE = 4;

    private MiningTier() {
    }

    public static boolean tierPermits(int materialLevel, int requiredLevel) {
        return materialLevel >= requiredLevel;
    }

    public static int requiredFor(boolean needsStone, boolean needsIron, boolean needsDiamond) {
        if (needsDiamond) {
            return DIAMOND;
        }
        if (needsIron) {
            return IRON;
        }
        if (needsStone) {
            return STONE;
        }
        return NONE;
    }
}

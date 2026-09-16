package teacommontea.veritechasse.Vanilla.Potions;

public final class RaidOmen {

    public static final String KEY = "raid_omen";
    public static final int VANILLA_MAX_AMPLIFIER = 4;
    public static final boolean BENEFICIAL = false;
    public static final boolean NEUTRAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final int DEFAULT_MAX_RAID_OMEN_LEVEL = 5;

    private RaidOmen() {
    }

    public static boolean appliesThisTick(int remainingDuration) {
        return remainingDuration == 1;
    }

    public static boolean triggersOnExpiry() {
        return true;
    }

    public static int raidOmenLevelAfter(int currentLevel, int amplifier, int maxRaidOmenLevel) {
        int raised = currentLevel + amplifier + 1;
        if (raised < 0) {
            return 0;
        }
        return raised > maxRaidOmenLevel ? maxRaidOmenLevel : raised;
    }

    public static int raidOmenLevelAfter(int currentLevel, int amplifier) {
        return raidOmenLevelAfter(currentLevel, amplifier, DEFAULT_MAX_RAID_OMEN_LEVEL);
    }

    public static int heroAmplifierFrom(int raidOmenLevel) {
        return raidOmenLevel - 1;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}

package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;

public final class BadOmen {

    public static final String KEY = "bad_omen";
    public static final int VANILLA_MAX_AMPLIFIER = 4;
    public static final boolean BENEFICIAL = false;
    public static final boolean NEUTRAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean APPLIES_EVERY_TICK = true;

    public static final int RAID_OMEN_DURATION_TICKS = 600;

    public static final int OMINOUS_BOTTLE_DURATION_TICKS = 120000;
    public static final int OMINOUS_BOTTLE_MIN_AMPLIFIER = 0;
    public static final int OMINOUS_BOTTLE_MAX_AMPLIFIER = VANILLA_MAX_AMPLIFIER;

    private BadOmen() {
    }

    public static final int OMINOUS_BOTTLE_MAJOR = 1;
    public static final int OMINOUS_BOTTLE_MINOR = 20;
    public static final int OMINOUS_BOTTLE_PATCH = 5;

    public static boolean ominousBottleExists(Protocol protocol) {
        return protocol.atLeast(OMINOUS_BOTTLE_MAJOR, OMINOUS_BOTTLE_MINOR, OMINOUS_BOTTLE_PATCH);
    }

    public static boolean grantedByOminousBottle(int amplifier, Protocol protocol) {
        if (!ominousBottleExists(protocol)) {
            return false;
        }
        return amplifier >= OMINOUS_BOTTLE_MIN_AMPLIFIER
            && amplifier <= OMINOUS_BOTTLE_MAX_AMPLIFIER;
    }

    public static int ominousBottleDurationTicks() {
        return OMINOUS_BOTTLE_DURATION_TICKS;
    }

    public static boolean convertsToRaidOmen(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static boolean triggersRaidDirectly(Protocol protocol) {
        return !convertsToRaidOmen(protocol);
    }

    public static boolean canTrigger(boolean spectator, boolean peaceful, boolean inVillage) {
        return !spectator && !peaceful && inVillage;
    }

    public static boolean canTrigger(
            boolean spectator,
            boolean peaceful,
            boolean inVillage,
            boolean raidPresent,
            int raidOmenLevel,
            int maxRaidOmenLevel) {
        if (!canTrigger(spectator, peaceful, inVillage)) {
            return false;
        }
        return !raidPresent || raidOmenLevel < maxRaidOmenLevel;
    }

    public static int raidOmenAmplifier(int amplifier) {
        return amplifier;
    }

    public static int raidOmenDurationTicks() {
        return RAID_OMEN_DURATION_TICKS;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}

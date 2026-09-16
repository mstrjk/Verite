package teacommontea.veritechasse.Vanilla.Tools.Trident.Support;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class TridentDurability {

    public static final int MAX_DURABILITY = 250;

    public static final int COST_MELEE_HIT = 1;
    public static final int COST_MINE_BLOCK = 2;
    public static final int COST_THROW = 1;
    public static final int COST_RIPTIDE = 1;

    private static final int NO_BREAK_MAJOR = 1;
    private static final int NO_BREAK_MINOR = 21;
    private static final int NO_BREAK_PATCH = 3;

    private TridentDurability() {
    }

    public static boolean throwCanBreak(Protocol protocol) {
        return protocol.below(NO_BREAK_MAJOR, NO_BREAK_MINOR, NO_BREAK_PATCH);
    }

    public static int remainingAfter(int currentDamage, int cost, Protocol protocol) {
        int next = currentDamage + cost;
        if (!throwCanBreak(protocol) && next >= MAX_DURABILITY) {
            return MAX_DURABILITY - 1;
        }
        return next;
    }

    public static boolean wouldBreak(int currentDamage, int cost) {
        return currentDamage + cost >= MAX_DURABILITY;
    }

    public static Reality minimumDamageAfter(int hits, int cost) {
        if (hits <= 0) {
            return Reality.impossible();
        }
        return Reality.of((double) hits * (double) cost);
    }

    public static int expectedDamageFor(int meleeHits, int blocksMined, int throwsMade) {
        return meleeHits * COST_MELEE_HIT
            + blocksMined * COST_MINE_BLOCK
            + throwsMade * COST_THROW;
    }

    public static boolean consumptionIsPossible(int observedDamageDelta, int meleeHits, int blocksMined, int throwsMade) {
        return observedDamageDelta >= expectedDamageFor(meleeHits, blocksMined, throwsMade);
    }
}

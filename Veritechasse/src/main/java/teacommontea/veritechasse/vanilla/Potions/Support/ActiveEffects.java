package teacommontea.veritechasse.Vanilla.Potions.Support;

import java.util.Locale;
import java.util.Map;

public final class ActiveEffects {

    public static final int ABSENT = -1;

    private final Map<String, Integer> amplifiers;

    private ActiveEffects(Map<String, Integer> amplifiers) {
        this.amplifiers = amplifiers;
    }

    public static ActiveEffects of(Map<String, Integer> amplifiers) {
        return new ActiveEffects(Map.copyOf(amplifiers));
    }

    public static ActiveEffects none() {
        return new ActiveEffects(Map.of());
    }

    public int amplifierOf(String key) {
        if (key == null) {
            return ABSENT;
        }
        Integer found = this.amplifiers.get(key.toLowerCase(Locale.ROOT));
        return found == null ? ABSENT : found.intValue();
    }

    public boolean has(String key) {
        return amplifierOf(key) != ABSENT;
    }

    public int level(String key) {
        int amplifier = amplifierOf(key);
        return amplifier == ABSENT ? 0 : amplifier + 1;
    }
}

package teacommontea.veritechasse.vanilla.Tools.Axes.Support;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class ShieldDisable {

    public static final int COOLDOWN_TICKS = 100;
    public static final float DISABLE_SECONDS = 5.0F;

    public static final float LEGACY_BASE_CHANCE = 0.25F;
    public static final float LEGACY_EFFICIENCY_CHANCE_PER_LEVEL = 0.05F;
    public static final float LEGACY_SPRINT_CHANCE_BONUS = 0.75F;

    private ShieldDisable() {
    }

    public static boolean isChanceBased(Protocol protocol) {
        return protocol.below(1, 20, 5);
    }

    public static float legacyChance(int efficiencyLevel, boolean sprinting) {
        float chance = LEGACY_BASE_CHANCE + (float) efficiencyLevel * LEGACY_EFFICIENCY_CHANCE_PER_LEVEL;
        if (sprinting) {
            chance = chance + LEGACY_SPRINT_CHANCE_BONUS;
        }
        return chance;
    }

    public static boolean legacyDisables(int efficiencyLevel, boolean sprinting, float roll) {
        return roll < legacyChance(efficiencyLevel, sprinting);
    }

    public static boolean guaranteed(Protocol protocol) {
        return !isChanceBased(protocol);
    }

    public static int cooldownTicks() {
        return COOLDOWN_TICKS;
    }

    public static Reality chanceFrom(int efficiencyLevel, boolean sprinting, Protocol protocol) {
        if (guaranteed(protocol)) {
            return Reality.of(1.0D);
        }
        float chance = legacyChance(efficiencyLevel, sprinting);
        return Reality.of(chance > 1.0F ? 1.0D : (double) chance);
    }
}

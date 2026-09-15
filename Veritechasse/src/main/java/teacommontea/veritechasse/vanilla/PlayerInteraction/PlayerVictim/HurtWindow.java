package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerVictim;

import teacommontea.veritechasse.vanilla.Protocol;

public final class HurtWindow {

    public static final String KEY = "hurt_window";

    public static final int INVULNERABLE_TICKS = 20;
    public static final int HURT_DURATION_TICKS = 10;

    public static final float PARTIAL_THRESHOLD = 10.0F;

    public static final float HELMET_DAMAGE_RETAINED = 0.75F;

    public static final int BYPASS_TAG_MAJOR = 1;
    public static final int BYPASS_TAG_MINOR = 20;
    public static final int BYPASS_TAG_PATCH = 0;

    private HurtWindow() {
    }

    public static boolean bypassTagExists(Protocol protocol) {
        return protocol.atLeast(BYPASS_TAG_MAJOR, BYPASS_TAG_MINOR, BYPASS_TAG_PATCH);
    }

    public static boolean inPartialWindow(int invulnerableTime) {
        return (float) invulnerableTime > PARTIAL_THRESHOLD;
    }

    public static boolean appliesPartialRule(
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        if (!inPartialWindow(invulnerableTime)) {
            return false;
        }
        if (bypassTagExists(protocol) && bypassesCooldown) {
            return false;
        }
        return true;
    }

    public static boolean absorbedEntirely(
            float incomingDamage,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        if (!appliesPartialRule(invulnerableTime, bypassesCooldown, protocol)) {
            return false;
        }
        return incomingDamage <= lastHurt;
    }

    public static float appliedDamage(
            float incomingDamage,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        if (!appliesPartialRule(invulnerableTime, bypassesCooldown, protocol)) {
            return incomingDamage;
        }
        if (incomingDamage <= lastHurt) {
            return 0.0F;
        }
        return incomingDamage - lastHurt;
    }

    public static float lastHurtAfter(
            float incomingDamage,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        if (absorbedEntirely(
                incomingDamage, lastHurt, invulnerableTime, bypassesCooldown, protocol)) {
            return lastHurt;
        }
        return incomingDamage;
    }

    public static int invulnerableTimeAfter(
            float incomingDamage,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        if (appliesPartialRule(invulnerableTime, bypassesCooldown, protocol)) {
            return invulnerableTime;
        }
        return INVULNERABLE_TICKS;
    }

    public static boolean refreshesTimer(
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        return !appliesPartialRule(invulnerableTime, bypassesCooldown, protocol);
    }

    public static float afterHelmetReduction(float damage, boolean damagesHelmet, boolean wearingHelmet) {
        if (!damagesHelmet || !wearingHelmet) {
            return damage;
        }
        return damage * HELMET_DAMAGE_RETAINED;
    }

    public static int minimumTicksBetweenFullHits() {
        return INVULNERABLE_TICKS - (int) PARTIAL_THRESHOLD;
    }
}

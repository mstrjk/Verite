package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerTeleport;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class TeleportEffects {

    public static final double ENDER_PEARL_DAMAGE = 5.0D;

    public static final int PLAYER_PORTAL_COOLDOWN_TICKS = 10;
    public static final int DEFAULT_PORTAL_COOLDOWN_TICKS = 300;
    public static final int END_GATEWAY_COOLDOWN_TICKS = 40;

    public static final double CLEARED_MOMENTUM = 0.0D;

    private TeleportEffects() {
    }

    public static boolean resetsFallDistance(String cause) {
        String key = TeleportCauses.normalise(cause);
        if (TeleportCauses.DISMOUNT.equals(key) || TeleportCauses.EXIT_BED.equals(key)) {
            return false;
        }
        return !TeleportCauses.isUnknown(key);
    }

    public static boolean clearsMomentum(String cause) {
        String key = TeleportCauses.normalise(cause);
        if (TeleportCauses.ENDER_PEARL.equals(key)) {
            return true;
        }
        return TeleportCauses.isExternal(key);
    }

    public static double horizontalAfter(String cause, double currentHorizontal) {
        return clearsMomentum(cause) ? CLEARED_MOMENTUM : currentHorizontal;
    }

    public static double verticalAfter(String cause, double currentVertical) {
        return clearsMomentum(cause) ? CLEARED_MOMENTUM : currentVertical;
    }

    public static boolean dealsDamage(String cause) {
        return TeleportCauses.ENDER_PEARL.equals(TeleportCauses.normalise(cause));
    }

    public static double damageOf(String cause) {
        return dealsDamage(cause) ? ENDER_PEARL_DAMAGE : 0.0D;
    }

    public static int portalCooldownTicks(boolean isPlayer) {
        return isPlayer ? PLAYER_PORTAL_COOLDOWN_TICKS : DEFAULT_PORTAL_COOLDOWN_TICKS;
    }

    public static int cooldownTicksFor(String cause, boolean isPlayer) {
        String key = TeleportCauses.normalise(cause);
        if (TeleportCauses.END_GATEWAY.equals(key)) {
            return END_GATEWAY_COOLDOWN_TICKS;
        }
        if (TeleportCauses.NETHER_PORTAL.equals(key) || TeleportCauses.END_PORTAL.equals(key)) {
            return portalCooldownTicks(isPlayer);
        }
        return 0;
    }

    public static boolean withinCooldown(String cause, int ticksSincePrevious, boolean isPlayer) {
        int cooldown = cooldownTicksFor(cause, isPlayer);
        if (cooldown <= 0) {
            return false;
        }
        return ticksSincePrevious < cooldown;
    }

    public static boolean repeatIsPossible(String cause, int ticksSincePrevious, boolean isPlayer) {
        return !withinCooldown(cause, ticksSincePrevious, isPlayer);
    }

    public static boolean enderPearlSurvivable(String cause, double health, double absorption) {
        if (!dealsDamage(cause)) {
            return true;
        }
        return health + absorption > ENDER_PEARL_DAMAGE;
    }

    public static boolean crossesDimensionLegally(String cause, Protocol protocol) {
        String key = TeleportCauses.normalise(cause);
        if (!TeleportCauses.exists(key, protocol)) {
            return false;
        }
        if (TeleportCauses.NETHER_PORTAL.equals(key)
            || TeleportCauses.END_PORTAL.equals(key)
            || TeleportCauses.END_GATEWAY.equals(key)) {
            return true;
        }
        return TeleportCauses.isExternal(key) || TeleportCauses.SPECTATE.equals(key);
    }

    public static Reality damageFrom(String cause) {
        return Reality.of(damageOf(cause));
    }
}

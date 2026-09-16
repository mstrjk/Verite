package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Riptide;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak.LookGeometry;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class RiptideImpulseXZ {

    public static final int SPIN_ATTACK_TICKS = 20;
    public static final float SPIN_ATTACK_DAMAGE = 8.0F;

    public static final boolean REQUIRES_WATER_OR_RAIN = true;
    public static final boolean BLOCKED_WHILE_PASSENGER = true;

    private RiptideImpulseXZ() {
    }

    public static final int PASSENGER_GATE_MAJOR = 26;
    public static final int PASSENGER_GATE_MINOR = 1;
    public static final int PASSENGER_GATE_PATCH = 0;

    public static boolean passengerBlocksLaunch(Protocol protocol) {
        return protocol.atLeast(
            PASSENGER_GATE_MAJOR, PASSENGER_GATE_MINOR, PASSENGER_GATE_PATCH);
    }

    public static boolean canLaunch(int riptideLevel, boolean inWaterOrRain, boolean passenger) {
        if (riptideLevel <= 0) {
            return false;
        }
        if (passenger) {
            return false;
        }
        return inWaterOrRain;
    }

    public static boolean canLaunch(
            int riptideLevel,
            boolean inWaterOrRain,
            boolean passenger,
            Protocol protocol) {
        if (riptideLevel <= 0) {
            return false;
        }
        if (passenger && passengerBlocksLaunch(protocol)) {
            return false;
        }
        return inWaterOrRain;
    }

    public static boolean canLaunch(ItemStack trident, boolean inWaterOrRain, boolean passenger) {
        if (trident == null) {
            return false;
        }
        return canLaunch(Riptide.levelOn(trident), inWaterOrRain, passenger);
    }

    public static float lookX(float yawDegrees, float pitchDegrees) {
        float yaw = yawDegrees * LookGeometry.DEGREES_TO_RADIANS;
        float pitch = pitchDegrees * LookGeometry.DEGREES_TO_RADIANS;
        return -LookGeometry.sin((double) yaw) * LookGeometry.cos((double) pitch);
    }

    public static float lookY(float pitchDegrees) {
        float pitch = pitchDegrees * LookGeometry.DEGREES_TO_RADIANS;
        return -LookGeometry.sin((double) pitch);
    }

    public static float lookZ(float yawDegrees, float pitchDegrees) {
        float yaw = yawDegrees * LookGeometry.DEGREES_TO_RADIANS;
        float pitch = pitchDegrees * LookGeometry.DEGREES_TO_RADIANS;
        return LookGeometry.cos((double) yaw) * LookGeometry.cos((double) pitch);
    }

    public static float lookLength(float yawDegrees, float pitchDegrees) {
        float xd = lookX(yawDegrees, pitchDegrees);
        float yd = lookY(pitchDegrees);
        float zd = lookZ(yawDegrees, pitchDegrees);
        return (float) Math.sqrt((double) (xd * xd + yd * yd + zd * zd));
    }

    public static double horizontalComponent(float yawDegrees, float pitchDegrees) {
        float dist = lookLength(yawDegrees, pitchDegrees);
        if (dist <= 0.0F) {
            return 0.0D;
        }
        float xd = lookX(yawDegrees, pitchDegrees) / dist;
        float zd = lookZ(yawDegrees, pitchDegrees) / dist;
        return Math.sqrt((double) (xd * xd + zd * zd));
    }

    public static double horizontalImpulse(float strength, float pitchDegrees) {
        return strength * Math.abs(Math.cos(Math.toRadians(pitchDegrees)));
    }

    public static double horizontalImpulse(ItemStack trident, float pitchDegrees, Era era) {
        if (trident == null) {
            return 0.0D;
        }
        int level = Riptide.levelOn(trident);
        if (level <= 0) {
            return 0.0D;
        }
        return horizontalImpulse(Riptide.spinAttackStrength(level, era), pitchDegrees);
    }

    public static double maximumHorizontalImpulse(ItemStack trident, Era era) {
        return horizontalImpulse(trident, 0.0F, era);
    }

    public static Reality impulseFrom(ItemStack trident, float pitchDegrees, boolean inWaterOrRain, boolean passenger, Era era) {
        if (trident == null || !canLaunch(trident, inWaterOrRain, passenger)) {
            return Reality.impossible();
        }
        return Reality.of(horizontalImpulse(trident, pitchDegrees, era));
    }

    public static Reality maximumFrom(ItemStack trident, Era era) {
        return Reality.of(maximumHorizontalImpulse(trident, era));
    }
}

package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak;

public final class LookGeometry {

    public static final String KEY = "look_geometry";

    public static final int SIN_TABLE_SIZE = 65536;
    public static final double SIN_TABLE_SCALE = 10430.378350470453D;
    public static final double SIN_TABLE_COS_OFFSET = 16384.0D;

    public static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0D);

    public static final boolean SERVER_VALIDATES_FACING = false;

    private static final float[] SIN = new float[SIN_TABLE_SIZE];

    static {
        for (int index = 0; index < SIN_TABLE_SIZE; index++) {
            SIN[index] = (float) Math.sin((double) index / SIN_TABLE_SCALE);
        }
    }

    private LookGeometry() {
    }

    public static float sin(double radians) {
        return SIN[(int) ((long) (radians * SIN_TABLE_SCALE) & (long) (SIN_TABLE_SIZE - 1))];
    }

    public static float cos(double radians) {
        return SIN[(int) ((long) (radians * SIN_TABLE_SCALE + SIN_TABLE_COS_OFFSET)
            & (long) (SIN_TABLE_SIZE - 1))];
    }

    public static double viewX(float xRot, float yRot) {
        float realXRot = xRot * DEGREES_TO_RADIANS;
        float realYRot = -yRot * DEGREES_TO_RADIANS;
        return (double) (sin((double) realYRot) * cos((double) realXRot));
    }

    public static double viewY(float xRot) {
        float realXRot = xRot * DEGREES_TO_RADIANS;
        return (double) (-sin((double) realXRot));
    }

    public static double viewZ(float xRot, float yRot) {
        float realXRot = xRot * DEGREES_TO_RADIANS;
        float realYRot = -yRot * DEGREES_TO_RADIANS;
        return (double) (cos((double) realYRot) * cos((double) realXRot));
    }

    public static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0D;
        if (wrapped >= 180.0D) {
            wrapped = wrapped - 360.0D;
        }
        if (wrapped < -180.0D) {
            wrapped = wrapped + 360.0D;
        }
        return wrapped;
    }

    public static double dotToBlockCentre(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            float xRot,
            float yRot) {
        double dx = (double) blockX + 0.5D - eyeX;
        double dy = (double) blockY + 0.5D - eyeY;
        double dz = (double) blockZ + 0.5D - eyeZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.0D) {
            return 1.0D;
        }
        return (dx * viewX(xRot, yRot)
            + dy * viewY(xRot)
            + dz * viewZ(xRot, yRot)) / length;
    }

    public static double angleToBlockCentre(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            float xRot,
            float yRot) {
        double dot = dotToBlockCentre(eyeX, eyeY, eyeZ, blockX, blockY, blockZ, xRot, yRot);
        if (dot > 1.0D) {
            dot = 1.0D;
        }
        if (dot < -1.0D) {
            dot = -1.0D;
        }
        return Math.acos(dot);
    }

    public static double maximumHalfAngle(double distance) {
        if (distance <= 0.0D) {
            return Math.PI;
        }
        double radius = Math.sqrt(3.0D) * 0.5D;
        if (radius >= distance) {
            return Math.PI;
        }
        return Math.asin(radius / distance);
    }

    public static boolean facingCouldReach(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            float xRot,
            float yRot) {
        double dx = (double) blockX + 0.5D - eyeX;
        double dy = (double) blockY + 0.5D - eyeY;
        double dz = (double) blockZ + 0.5D - eyeZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double angle = angleToBlockCentre(eyeX, eyeY, eyeZ, blockX, blockY, blockZ, xRot, yRot);
        return angle <= maximumHalfAngle(distance);
    }

    public static boolean serverValidatesFacing() {
        return SERVER_VALIDATES_FACING;
    }
}

package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger;

public final class Exhaustion {

    public static final String KEY = "exhaustion";

    public static final float CENTIMETRES_PER_BLOCK = 100.0F;
    public static final float PER_CENTIMETRE = 0.01F;

    public static final float SPRINT_PER_BLOCK = 0.1F;
    public static final float SWIM_PER_BLOCK = 0.01F;
    public static final float UNDERWATER_WALK_PER_BLOCK = 0.01F;
    public static final float ON_WATER_WALK_PER_BLOCK = 0.01F;
    public static final float WALK_PER_BLOCK = 0.0F;
    public static final float CROUCH_PER_BLOCK = 0.0F;
    public static final float CLIMB_PER_BLOCK = 0.0F;
    public static final float FLY_PER_BLOCK = 0.0F;

    public static final float JUMP = 0.05F;
    public static final float SPRINT_JUMP = 0.2F;
    public static final float ATTACK = 0.1F;
    public static final float MINE = 0.005F;
    public static final float HEAL = 6.0F;

    private Exhaustion() {
    }

    public static int centimetres(double dx, double dy, double dz) {
        return Math.round((float) Math.sqrt(dx * dx + dy * dy + dz * dz)
            * CENTIMETRES_PER_BLOCK);
    }

    public static int centimetresHorizontal(double dx, double dz) {
        return Math.round((float) Math.sqrt(dx * dx + dz * dz)
            * CENTIMETRES_PER_BLOCK);
    }

    public static float overDistance(float perBlock, int centimetres) {
        if (centimetres <= 0) {
            return 0.0F;
        }
        return perBlock * (float) centimetres * PER_CENTIMETRE;
    }

    public static float swimming(double dx, double dy, double dz) {
        return overDistance(SWIM_PER_BLOCK, centimetres(dx, dy, dz));
    }

    public static float underwater(double dx, double dy, double dz) {
        return overDistance(UNDERWATER_WALK_PER_BLOCK, centimetres(dx, dy, dz));
    }

    public static float onWater(double dx, double dz) {
        return overDistance(ON_WATER_WALK_PER_BLOCK, centimetresHorizontal(dx, dz));
    }

    public static float sprinting(double dx, double dz) {
        return overDistance(SPRINT_PER_BLOCK, centimetresHorizontal(dx, dz));
    }

    public static float walking(double dx, double dz) {
        return overDistance(WALK_PER_BLOCK, centimetresHorizontal(dx, dz));
    }

    public static float crouching(double dx, double dz) {
        return overDistance(CROUCH_PER_BLOCK, centimetresHorizontal(dx, dz));
    }

    public static float jumping(boolean sprinting) {
        return sprinting ? SPRINT_JUMP : JUMP;
    }

    public static float movement(
            double dx,
            double dy,
            double dz,
            boolean riding,
            boolean swimming,
            boolean eyeInWater,
            boolean inWater,
            boolean climbing,
            boolean onGround,
            boolean sprinting,
            boolean crouching) {
        if (riding) {
            return 0.0F;
        }
        if (swimming) {
            return swimming(dx, dy, dz);
        }
        if (eyeInWater) {
            return underwater(dx, dy, dz);
        }
        if (inWater) {
            return onWater(dx, dz);
        }
        if (climbing) {
            return 0.0F;
        }
        if (!onGround) {
            return 0.0F;
        }
        if (sprinting) {
            return sprinting(dx, dz);
        }
        return crouching ? crouching(dx, dz) : walking(dx, dz);
    }
}

package teacommontea.veritechasse.Vanilla.Tools.Ranged.Support;

public final class ProjectileSpread {

    private ProjectileSpread() {
    }

    public static float angleStep(int projectileCount, float maxAngle) {
        if (projectileCount <= 1) {
            return 0.0F;
        }
        return 2.0F * maxAngle / (float) (projectileCount - 1);
    }

    public static float angleOffset(int projectileCount, float angleStep) {
        return (float) ((projectileCount - 1) % 2) * angleStep / 2.0F;
    }

    public static float[] angles(int projectileCount, float maxAngle) {
        float[] out = new float[projectileCount];
        float step = angleStep(projectileCount, maxAngle);
        float offset = angleOffset(projectileCount, step);
        float direction = 1.0F;

        for (int i = 0; i < projectileCount; i++) {
            out[i] = offset + direction * (float) ((i + 1) / 2) * step;
            direction = -direction;
        }

        return out;
    }

    public static float widestAngle(int projectileCount, float maxAngle) {
        float widest = 0.0F;
        for (float angle : angles(projectileCount, maxAngle)) {
            float magnitude = angle < 0.0F ? -angle : angle;
            if (magnitude > widest) {
                widest = magnitude;
            }
        }
        return widest;
    }
}

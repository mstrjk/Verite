package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerPlace;

import java.util.Locale;

public final class BlockFace {

    public static final String KEY = "block_face";

    public static final String DOWN = "down";
    public static final String UP = "up";
    public static final String NORTH = "north";
    public static final String SOUTH = "south";
    public static final String WEST = "west";
    public static final String EAST = "east";

    public static final int DOWN_ORDINAL = 0;
    public static final int UP_ORDINAL = 1;
    public static final int NORTH_ORDINAL = 2;
    public static final int SOUTH_ORDINAL = 3;
    public static final int WEST_ORDINAL = 4;
    public static final int EAST_ORDINAL = 5;

    public static final String AXIS_X = "x";
    public static final String AXIS_Y = "y";
    public static final String AXIS_Z = "z";

    public static final double HIT_LOCATION_LIMIT = 1.0000001D;

    private BlockFace() {
    }

    public static boolean isFace(String face) {
        return ordinalOf(face) >= 0;
    }

    public static int ordinalOf(String face) {
        if (face == null) {
            return -1;
        }
        switch (face.toLowerCase(Locale.ROOT)) {
            case DOWN:
                return DOWN_ORDINAL;
            case UP:
                return UP_ORDINAL;
            case NORTH:
                return NORTH_ORDINAL;
            case SOUTH:
                return SOUTH_ORDINAL;
            case WEST:
                return WEST_ORDINAL;
            case EAST:
                return EAST_ORDINAL;
            default:
                return -1;
        }
    }

    public static String opposite(String face) {
        if (face == null) {
            return null;
        }
        switch (face.toLowerCase(Locale.ROOT)) {
            case DOWN:
                return UP;
            case UP:
                return DOWN;
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
            case EAST:
                return WEST;
            default:
                return null;
        }
    }

    public static String axisOf(String face) {
        if (face == null) {
            return null;
        }
        switch (face.toLowerCase(Locale.ROOT)) {
            case DOWN:
            case UP:
                return AXIS_Y;
            case NORTH:
            case SOUTH:
                return AXIS_Z;
            case WEST:
            case EAST:
                return AXIS_X;
            default:
                return null;
        }
    }

    public static int stepX(String face) {
        if (EAST.equals(lower(face))) {
            return 1;
        }
        return WEST.equals(lower(face)) ? -1 : 0;
    }

    public static int stepY(String face) {
        if (UP.equals(lower(face))) {
            return 1;
        }
        return DOWN.equals(lower(face)) ? -1 : 0;
    }

    public static int stepZ(String face) {
        if (SOUTH.equals(lower(face))) {
            return 1;
        }
        return NORTH.equals(lower(face)) ? -1 : 0;
    }

    private static String lower(String face) {
        return face == null ? null : face.toLowerCase(Locale.ROOT);
    }

    public static int relativeX(int blockX, String face) {
        return blockX + stepX(face);
    }

    public static int relativeY(int blockY, String face) {
        return blockY + stepY(face);
    }

    public static int relativeZ(int blockZ, String face) {
        return blockZ + stepZ(face);
    }

    public static boolean hitLocationIsWithinBlock(
            double hitX,
            double hitY,
            double hitZ,
            int blockX,
            int blockY,
            int blockZ) {
        double dx = hitX - ((double) blockX + 0.5D);
        double dy = hitY - ((double) blockY + 0.5D);
        double dz = hitZ - ((double) blockZ + 0.5D);
        return Math.abs(dx) < HIT_LOCATION_LIMIT
            && Math.abs(dy) < HIT_LOCATION_LIMIT
            && Math.abs(dz) < HIT_LOCATION_LIMIT;
    }

    public static boolean hitLocationMatchesFace(
            double hitX,
            double hitY,
            double hitZ,
            int blockX,
            int blockY,
            int blockZ,
            String face,
            double tolerance) {
        String axis = axisOf(face);
        if (axis == null) {
            return false;
        }
        if (AXIS_Y.equals(axis)) {
            double plane = (double) blockY + (stepY(face) > 0 ? 1.0D : 0.0D);
            return Math.abs(hitY - plane) <= tolerance;
        }
        if (AXIS_X.equals(axis)) {
            double plane = (double) blockX + (stepX(face) > 0 ? 1.0D : 0.0D);
            return Math.abs(hitX - plane) <= tolerance;
        }
        double plane = (double) blockZ + (stepZ(face) > 0 ? 1.0D : 0.0D);
        return Math.abs(hitZ - plane) <= tolerance;
    }
}

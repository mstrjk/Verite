package teacommontea.veritechasse.reality;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import teacommontea.veritechasse.vanilla.PlayerMovement.Support.GroundState;

public final class SupportingBlock {

    public static final double HORIZONTAL_INSET = 1.0E-3D;

    public static final double VANILLA_PROBE_DEPTH = GroundState.SUPPORT_SEARCH_DEPTH;

    public static final double PROBE_DEPTH = 1.0E-3D;

    private SupportingBlock() {
    }

    public static boolean present(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        BoundingBox box = player.getBoundingBox();
        return presentBeneath(world, box);
    }

    public static boolean presentBeneath(World world, BoundingBox box) {
        double minX = box.getMinX() + HORIZONTAL_INSET;
        double maxX = box.getMaxX() - HORIZONTAL_INSET;
        double minZ = box.getMinZ() + HORIZONTAL_INSET;
        double maxZ = box.getMaxZ() - HORIZONTAL_INSET;
        double top = box.getMinY();
        double bottom = top - PROBE_DEPTH;

        BoundingBox probe = new BoundingBox(minX, bottom, minZ, maxX, top, maxZ);

        int fromX = floor(minX);
        int toX = floor(maxX);
        int fromZ = floor(minZ);
        int toZ = floor(maxZ);
        int fromY = floor(bottom);
        int toY = floor(top);

        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = fromY; y <= toY; y++) {
                    if (supports(world.getBlockAt(x, y, z), probe)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean supports(Block block, BoundingBox probe) {
        if (block.isEmpty() || block.isPassable()) {
            return false;
        }
        for (BoundingBox part : block.getCollisionShape().getBoundingBoxes()) {
            BoundingBox world = part.clone().shift(block.getLocation());
            if (world.overlaps(probe)) {
                return true;
            }
        }
        return false;
    }

    private static int floor(double value) {
        int truncated = (int) value;
        return value < (double) truncated ? truncated - 1 : truncated;
    }
}

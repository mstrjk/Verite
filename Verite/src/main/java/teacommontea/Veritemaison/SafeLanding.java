package teacommontea.veritemaison;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

final class SafeLanding {

    private static final Set<Material> HAZARDS = EnumSet.of(
            Material.LAVA, Material.FIRE, Material.SOUL_FIRE, Material.MAGMA_BLOCK,
            Material.CACTUS, Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.SWEET_BERRY_BUSH,
            Material.POWDER_SNOW, Material.WITHER_ROSE, Material.POINTED_DRIPSTONE);

    private SafeLanding() {}

    static boolean grounded(Entity target) {
        return target instanceof Player player
                && !player.isFlying()
                && player.getGameMode() != GameMode.CREATIVE;
    }

    static Location below(Location from) {
        World world = from.getWorld();
        if (world == null) {
            return from.clone();
        }
        int min = world.getMinHeight() + 1;
        int max = world.getMaxHeight() - 2;
        int start = Math.max(min, Math.min(max, from.getBlockY()));
        for (int y = start; y >= min; y--) {
            Double standing = standingHeight(world, from.getBlockX(), y, from.getBlockZ());
            if (standing != null) {
                return at(from, standing);
            }
        }
        for (int y = start + 1; y <= max; y++) {
            Double standing = standingHeight(world, from.getBlockX(), y, from.getBlockZ());
            if (standing != null) {
                return at(from, standing);
            }
        }
        return from.clone();
    }

    private static Location at(Location from, double y) {
        Location out = from.clone();
        out.setY(y);
        return out;
    }

    private static Double standingHeight(World world, int x, int y, int z) {
        Block ground = world.getBlockAt(x, y - 1, z);
        if (!ground.getType().isSolid() || HAZARDS.contains(ground.getType())) {
            return null;
        }
        double top = Math.max(y, ground.getBoundingBox().getMaxY());
        int clearTo = (int) Math.ceil(top + 1.8);
        for (int by = y; by < clearTo; by++) {
            if (!open(world.getBlockAt(x, by, z))) {
                return null;
            }
        }
        return top;
    }

    private static boolean open(Block block) {
        return block.isPassable() && !HAZARDS.contains(block.getType()) && block.getType() != Material.WATER;
    }
}

package dev.nullman.garticphone.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class ArenaUtils {
    public static void createArenaFrame(Location loc1, Location loc2, Material wallBlock, Material floorBlock) {
        World world = loc1.getWorld();
        if (world == null) return;

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isWall = (x == minX || x == maxX || z == minZ || z == maxZ);
                    boolean isFloor = (y == minY);

                    if (isWall && y >= minY) {
                        world.getBlockAt(x, y, z).setType(wallBlock);
                    } else if (isFloor) {
                        world.getBlockAt(x, y, z).setType(floorBlock);
                    } else {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }

}

package dev.nullman.garticphone.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class RegionSnapshot {

    private final World world;
    private final int x1, y1, z1;
    private final int x2, y2, z2;
    private final List<BlockData> blocks = new ArrayList<>();

    public RegionSnapshot(World world, Location loc1, Location loc2) {
        this.world = world;

        this.x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
        this.y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
        this.z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        this.x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
        this.y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
        this.z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        capture();
    }

    private void capture() {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(new BlockData(x, y, z, block.getType().name(), block.getBlockData().getAsString()));
                }
            }
        }
    }

    public void restore() {
        for (BlockData data : blocks) {
            Block block = world.getBlockAt(data.x, data.y, data.z);
            block.setType(Bukkit.createBlockData(data.blockData).getMaterial(), false);
            block.setBlockData(Bukkit.createBlockData(data.blockData), false);
        }
    }

    private static class BlockData {
        int x, y, z;
        String material;
        String blockData;

        BlockData(int x, int y, int z, String material, String blockData) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
            this.blockData = blockData;
        }
    }
}


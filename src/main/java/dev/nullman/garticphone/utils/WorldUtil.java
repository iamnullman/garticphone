package dev.nullman.garticphone.utils;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
@Setter
public class WorldUtil {
    private World world;

    private static final NamespacedKey EMPTY_WORLD_KEY = new NamespacedKey("garticphone", "is_empty_world");

    public WorldUtil(String worldName) {
        this.world = Bukkit.getWorld(worldName);
        if (this.world == null) {
            WorldCreator creator = new WorldCreator(worldName);
            creator.generator(new EmptyChunkGenerator());
            creator.type(WorldType.FLAT);

            World newWorld = creator.createWorld();

            if (newWorld != null) {
                this.world = newWorld;
                Bukkit.getLogger().info("World created and initialized: " + worldName);
            } else {
                Bukkit.getLogger().severe("Failed to create world: " + worldName);
            }
        }else {
            Bukkit.getLogger().info("World loaded and initialized: " + worldName);
        }
    }

    public World getWorld() {
        if (this.world == null) throw new IllegalStateException("World is not initialized.");
        return this.world;
    }

    public void unloadWorld() {
        if (this.world != null) {
            Bukkit.unloadWorld(this.world, false);
            this.world = null;
        }
    }

    public Location getNextEmptyChunk() {
        if (this.world == null) throw new IllegalStateException("World is not initialized.");

        int range = 1000;
        int spacing = 16 * 16;

        for (int x = -range; x <= range; x += spacing) {
            for (int z = -range; z <= range; z += spacing) {
                Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
                if (!chunk.isLoaded()) chunk.load(true);

                if (isChunkMarkedPersistent(chunk)) continue;

                if (chunk.getEntities().length == 0 && chunk.getTileEntities().length == 0) {
                    chunk.getPersistentDataContainer().set(EMPTY_WORLD_KEY, PersistentDataType.STRING, "oynandi");

                    int y = world.getHighestBlockYAt(x, z);
                    return new Location(world, x + 8, y, z + 8);
                }
            }
        }

        // Boş chunk bulunamadı, yeni bir chunk oluştur ve işaretle
        int x = 0;
        int z = 0;
        // Son çare olarak, kullanılmamış bir bölge bul
        while (true) {
            // Rastgele koordinatlar seç
            x = (int) (Math.random() * 2000 - 1000);
            z = (int) (Math.random() * 2000 - 1000);

            // 16'nın katlarına yuvarla (chunk sınırlarına)
            x = (x >> 4) << 4;
            z = (z >> 4) << 4;

            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) chunk.load(true);

            if (!isChunkMarkedPersistent(chunk)) {
                chunk.getPersistentDataContainer().set(EMPTY_WORLD_KEY, PersistentDataType.STRING, "oynandi");
                int y = world.getHighestBlockYAt(x, z);
                return new Location(world, x + 8, y, z + 8);
            }
        }
    }


    private boolean isChunkMarkedPersistent(Chunk chunk) {
        Block baseBlock = chunk.getBlock(0, world.getMinHeight(), 0);

        PersistentDataContainer container = baseBlock.getChunk().getPersistentDataContainer();
        return container.has(EMPTY_WORLD_KEY, PersistentDataType.STRING);
    }

}

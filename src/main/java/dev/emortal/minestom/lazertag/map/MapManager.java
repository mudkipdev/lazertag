package dev.emortal.minestom.lazertag.map;

import dev.emortal.tnt.TNTLoader;
import dev.emortal.tnt.source.FileTNTSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);

    private static final DimensionType DIMENSION_TYPE = DimensionType.builder(NamespaceID.from("emortalmc:lazertag"))
            .skylightEnabled(true)
//            .ambientLight(1.0f)
            .build();

    private static final List<String> ENABLED_MAPS = List.of(
            "dizzymc"
    );
    private static final Path MAPS_PATH = Path.of("maps");

    private final Map<String, TNTLoader> mapLoaders;

    public MapManager() {
        MinecraftServer.getDimensionTypeManager().addDimension(DIMENSION_TYPE);

        Map<String, TNTLoader> chunkLoaders = new HashMap<>();

        for (String mapName : ENABLED_MAPS) {
            final Path tntPath = MAPS_PATH.resolve(mapName + ".tnt");
            final Path anvilPath = MAPS_PATH.resolve(mapName + "/");

            try {
//                PolarWorld world;
//                if (!Files.exists(polarPath)) { // File needs to be converted
//                    world = AnvilPolar.anvilToPolar(anvilPath, ChunkSelector.radius(5));
//                    PolarWriter.write(world);
//                } else {
//                    world = PolarReader.read(Files.readAllBytes(polarPath));
//                }
//
//                final PolarLoader chunkLoader = new PolarLoader(world);
                final TNTLoader chunkLoader = new TNTLoader(new FileTNTSource(tntPath));

                chunkLoaders.put(mapName, chunkLoader);
            } catch (IOException | NBTException e) {
                throw new RuntimeException(e);
            }
        }

        this.mapLoaders = Map.copyOf(chunkLoaders);
    }

    public @NotNull Instance getMap(@Nullable String id) {
        if (id == null) return this.getRandomMap();

        final TNTLoader chunkLoader = this.mapLoaders.get(id);
        if (chunkLoader == null) {
            LOGGER.warn("Map {} not found, loading random map", id);
            return this.getRandomMap();
        }

        LOGGER.info("Creating instance for map {}", id);

        return MinecraftServer.getInstanceManager().createInstanceContainer(DIMENSION_TYPE, chunkLoader);
    }

    public @NotNull Instance getRandomMap() {
        final String randomMapId = ENABLED_MAPS.get(ThreadLocalRandom.current().nextInt(ENABLED_MAPS.size()));
        final TNTLoader chunkLoader = this.mapLoaders.get(randomMapId);

        return MinecraftServer.getInstanceManager()
                .createInstanceContainer(DIMENSION_TYPE, chunkLoader);
    }

    public String getNameWithoutExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        return (dotIndex == -1) ? path : path.substring(0, dotIndex);
    }
}

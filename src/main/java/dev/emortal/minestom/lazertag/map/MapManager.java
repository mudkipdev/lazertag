package dev.emortal.minestom.lazertag.map;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MapManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapManager.class);
    private static final Gson GSON = new Gson();

    private static final List<String> ENABLED_MAPS = List.of(
            "dizzymc"
    );
    private static final Path MAPS_PATH = Path.of("maps");

    private static final int CHUNK_LOADING_RADIUS = 5;

    private final @NotNull Map<String, PreLoadedMap> preLoadedMaps;

    public MapManager() {
        DynamicRegistry.Key<DimensionType> dimension = MinecraftServer.getDimensionTypeRegistry().register(
                "emortalmc:lazertag",
                DimensionType.builder()
                        .hasSkylight(true)
                        .build());

        Map<String, PreLoadedMap> maps = new HashMap<>();
        for (String mapName : ENABLED_MAPS) {
            Path mapPath = MAPS_PATH.resolve(mapName);
            Path polarPath = mapPath.resolve("map.polar");
            Path dataPath = mapPath.resolve("data.json");

            try {
                MapData data = GSON.fromJson(new JsonReader(Files.newBufferedReader(dataPath)), MapData.class);
                LOGGER.debug("Loading data for map {}: [{}]", mapName, data);

                PolarLoader polarLoader = new PolarLoader(polarPath);
                InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(dimension, polarLoader);

                instance.setTimeRate(0);

                // Do some preloading!
                for (int x = -CHUNK_LOADING_RADIUS; x < CHUNK_LOADING_RADIUS; x++) {
                    for (int z = -CHUNK_LOADING_RADIUS; z < CHUNK_LOADING_RADIUS; z++) {
                        instance.loadChunk(x, z);
                    }
                }

                maps.put(mapName, new PreLoadedMap(instance, data));
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }

        this.preLoadedMaps = Map.copyOf(maps);
    }

    public @NotNull LoadedMap getMap(@Nullable String id) {
        if (id == null) {
            return this.getRandomMap();
        }

        PreLoadedMap map = this.preLoadedMaps.get(id);
        if (map == null) {
            LOGGER.warn("Map {} not found, loading random map", id);
            return this.getRandomMap();
        }

        return map.load();
    }

    public @NotNull LoadedMap getRandomMap() {
        String randomMapId = ENABLED_MAPS.get(ThreadLocalRandom.current().nextInt(ENABLED_MAPS.size()));
        PreLoadedMap map = this.preLoadedMaps.get(randomMapId);
        return map.load();
    }

    private record PreLoadedMap(@NotNull InstanceContainer rootInstance, @NotNull MapData data) {
        @NotNull LoadedMap load() {
            Instance shared = MinecraftServer.getInstanceManager().createSharedInstance(this.rootInstance());
            shared.setTimeRate(0);
            return new LoadedMap(shared, this.data());
        }
    }
}

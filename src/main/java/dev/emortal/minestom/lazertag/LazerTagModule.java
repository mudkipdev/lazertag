package dev.emortal.minestom.lazertag;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import dev.emortal.minestom.core.module.Module;
import dev.emortal.minestom.core.module.ModuleEnvironment;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import dev.emortal.minestom.lazertag.config.MapConfigJson;
import dev.emortal.minestom.lazertag.game.LazerTagGame;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import dev.emortal.minestom.lazertag.map.MapManager;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LazerTagModule extends Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(LazerTagModule.class);
    private static final Gson GSON = new Gson();

    public static Map<String, MapConfigJson> MAP_CONFIG_MAP;

    public LazerTagModule(final @NotNull ModuleEnvironment environment) {
        super(environment);
    }

    @Override
    public boolean onLoad() {

        InputStream is = getClass().getClassLoader().getResourceAsStream("maps.json");
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Type type = new TypeToken<HashMap<String, MapConfigJson>>(){}.getType();
        MAP_CONFIG_MAP = GSON.fromJson(reader, type);

        MapManager mapManager = new MapManager();

        GameSdkModule.init(
                new GameSdkConfig.Builder()
                        .minPlayers(LazerTagGame.MINIMUM_PLAYERS)
                        .maxGames(10)
                        .gameSupplier((info, node) -> new LazerTagGame(info, node, mapManager.getMap(info.mapId())))
                        .build()
        );

        return true;
    }

    @Override
    public void onUnload() {
    }
}

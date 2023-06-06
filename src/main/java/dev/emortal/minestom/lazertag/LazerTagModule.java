package dev.emortal.minestom.lazertag;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import dev.emortal.api.modules.Module;
import dev.emortal.api.modules.ModuleData;
import dev.emortal.api.modules.ModuleEnvironment;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import dev.emortal.minestom.lazertag.config.MapConfigJson;
import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.map.MapManager;
import dev.emortal.minestom.lazertag.raycast.RaycastUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ModuleData(name = "lazertag", softDependencies = {GameSdkModule.class}, required = true)
public final class LazerTagModule extends Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(LazerTagModule.class);
    private static final Gson GSON = new Gson();

    public static Map<String, MapConfigJson> MAP_CONFIG_MAP;

    public LazerTagModule(final @NotNull ModuleEnvironment environment) {
        super(environment);
    }

    @Override
    public boolean onLoad() {
        RaycastUtil.init();

        InputStream is = getClass().getClassLoader().getResourceAsStream("lazertag.json");
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

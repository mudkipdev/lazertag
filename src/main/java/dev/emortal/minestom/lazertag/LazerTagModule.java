package dev.emortal.minestom.lazertag;

import com.google.gson.Gson;
import dev.emortal.minestom.core.module.Module;
import dev.emortal.minestom.core.module.ModuleEnvironment;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameSdkConfig;
import dev.emortal.minestom.lazertag.config.ConfigLoader;
import dev.emortal.minestom.lazertag.game.LazerTagGame;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LazerTagModule extends Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(LazerTagModule.class);
    private static final Gson GSON = new Gson();

    private Map<String, Pos[]> spawnPositions;

    public LazerTagModule(final @NotNull ModuleEnvironment environment) {
        super(environment);
    }

    @Override
    public boolean onLoad() {
        // Since this should be the only thing on the server, we don't really need a separate folder to store its data.
        final ConfigLoader loader = new ConfigLoader(Path.of("."), GSON);
        try {
            spawnPositions = loader.loadSpawnPositions();
        } catch (final IOException exception) {
            LOGGER.error("Failed to load spawn positions!", exception);
            return false;
        }

        GameSdkModule.init(
                new GameSdkConfig.Builder()
                        .minPlayers(LazerTagGame.MINIMUM_PLAYERS)
                        .maxGames(10)
                        .gameSupplier(LazerTagGame::new)
                        .build()
        );

        return true;
    }

    @Override
    public void onUnload() {
    }
}

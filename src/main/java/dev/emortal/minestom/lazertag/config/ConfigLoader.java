package dev.emortal.minestom.lazertag.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minestom.server.coordinate.Pos;

public final class ConfigLoader {

    private final Path folder;
    private final Gson gson;

    public ConfigLoader(final Path folder, final Gson gson) {
        this.folder = folder;
        this.gson = gson;
    }

    public Map<String, Pos[]> loadSpawnPositions() throws IOException {
        final Path configFile = loadOrCreateConfigFile();
        final InputStream input = Files.newInputStream(configFile);
        final Type type = new TypeToken<Map<String, Pos[]>>() {}.getType();
        return gson.fromJson(new InputStreamReader(input), type);
    }

    private Path loadOrCreateConfigFile() throws IOException {
        if (!Files.exists(folder)) Files.createDirectory(folder);

        final var configFile = folder.resolve("lazertag.json");
        if (!Files.exists(configFile)) {
            try (final OutputStream output = Files.newOutputStream(configFile)) {
                try (final InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("lazertag.json")) {
                    input.transferTo(output);
                }
            }
        }
        return configFile;
    }
}

package dev.emortal.minestom.lazertag;

import dev.emortal.minestom.core.MinestomServer;
import dev.emortal.minestom.gamesdk.GameSdkModule;

public final class Entrypoint {

    public static void main(final String[] args) {
        new MinestomServer.Builder()
                .commonModules()
                .module(GameSdkModule.class, GameSdkModule::new)
                .module(LazerTagModule.class, LazerTagModule::new)
                .build();
    }
}

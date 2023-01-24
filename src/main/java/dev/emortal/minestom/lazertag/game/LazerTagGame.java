package dev.emortal.minestom.lazertag.game;

import dev.emortal.minestom.gamesdk.config.GameCreationInfo;
import dev.emortal.minestom.gamesdk.game.Game;
import org.jetbrains.annotations.NotNull;

public final class LazerTagGame extends Game {

    public static final int MINIMUM_PLAYERS = 2;

    public LazerTagGame(final @NotNull GameCreationInfo info) {
        super(info);
    }

    @Override
    public void load() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public void fastStart() {
    }
}

package dev.emortal.minestom.lazertag.config;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public record MapConfig(@NotNull Pos[] spawns) {
}

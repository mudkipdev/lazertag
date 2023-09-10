package dev.emortal.minestom.lazertag.map;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MapData(@NotNull List<Pos> spawns) {
}

package dev.emortal.minestom.lazertag.game;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TagHandler {

    public static void initializePlayerTags(@NotNull Player player) {
        player.setTag(DamageHandler.KILLS_TAG, 0);
        player.setTag(DamageHandler.DEATHS_TAG, 0);
        player.setTag(DamageHandler.COMBO_TAG, 0);
        player.setTag(DamageHandler.SPAWN_PROT_TAG, 0L);
    }

    public static void removePlayerTags(@NotNull Player player) {
        player.removeTag(DamageHandler.KILLS_TAG);
        player.removeTag(DamageHandler.DEATHS_TAG);
        player.removeTag(DamageHandler.COMBO_TAG);
        player.removeTag(DamageHandler.SPAWN_PROT_TAG);
    }

    private TagHandler() {
    }
}

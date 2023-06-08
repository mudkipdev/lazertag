package dev.emortal.minestom.lazertag.game;

import net.minestom.server.entity.Player;

public class TagHandler {

    public void initializePlayerTags(Player player) {
        player.setTag(DamageHandler.KILLS_TAG, 0);
        player.setTag(DamageHandler.DEATHS_TAG, 0);
        player.setTag(DamageHandler.COMBO_TAG, 0);
        player.setTag(DamageHandler.COMBO_MILLIS_TAG, 0L);
        player.setTag(DamageHandler.SPAWN_PROT_TAG, 0L);
    }

    public void removePlayerTags(Player player) {
        player.removeTag(DamageHandler.KILLS_TAG);
        player.removeTag(DamageHandler.DEATHS_TAG);
        player.removeTag(DamageHandler.COMBO_TAG);
        player.removeTag(DamageHandler.COMBO_MILLIS_TAG);
        player.removeTag(DamageHandler.SPAWN_PROT_TAG);
    }
}

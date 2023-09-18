package dev.emortal.minestom.lazertag.gun.guns;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.Gun;
import dev.emortal.minestom.lazertag.gun.GunItemInfo;
import dev.emortal.minestom.lazertag.gun.ItemRarity;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public final class Rifle extends Gun {
    private static final GunItemInfo INFO = new GunItemInfo(
            Material.STONE_HOE,
            ItemRarity.RARE,

            7f,
            80,
            0,
            15,

            2500,
            300,
            0,
            0,
            1,

            Sound.sound(SoundEvent.ENTITY_PLAYER_BIG_FALL, Sound.Source.PLAYER, 1f, 0.75f)
    );

    public Rifle(LazerTagGame game) {
        super(game, "Rifle", INFO);
    }
}

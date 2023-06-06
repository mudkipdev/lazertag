package dev.emortal.minestom.lazertag.gun.guns;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.Gun;
import dev.emortal.minestom.lazertag.gun.GunItemInfo;
import dev.emortal.minestom.lazertag.gun.ItemRarity;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class LazerMinigun extends Gun {
    public LazerMinigun(LazerTagGame game) {
        super(
                game,
                "Lazer Minigun",

                new GunItemInfo(
                        Material.NETHERITE_SHOVEL,
                        ItemRarity.RARE,

                        0.75f,
                        50.0,
                        3,
                        70,

                        3500,
                        100,
                        50,
                        0,
                        1,

                        Sound.sound(SoundEvent.BLOCK_BEACON_DEACTIVATE, Sound.Source.PLAYER, 2f, 2f)
                )
        );
    }
}

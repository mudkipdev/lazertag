package dev.emortal.minestom.lazertag.gun.guns;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.Gun;
import dev.emortal.minestom.lazertag.gun.GunItemInfo;
import dev.emortal.minestom.lazertag.gun.ItemRarity;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

public class Shotgun extends Gun {
    public Shotgun(LazerTagGame game) {
        super(
                game,
                "Shotgun",

                new GunItemInfo(
                        Material.REPEATER,
                        ItemRarity.RARE,

                        1.25f,
                        25.0,
                        1,
                        6,

                        3300,
                        400,
                        0,
                        0.13,
                        20,

                        Sound.sound(SoundEvent.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, Sound.Source.PLAYER, 2f, 1f)
                )
        );
    }

    @Override
    public void shoot(Player shooter) {
        super.shoot(shooter);
        shooter.scheduler().buildTask(() -> playReloadSound(shooter)).delay(TaskSchedule.tick(200 / MinecraftServer.TICK_MS)).schedule();
        shooter.setVelocity(shooter.getPosition().direction().mul(-15)); // TODO: Make sure this is TPS independant
    }
}

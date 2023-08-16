package dev.emortal.minestom.lazertag.gun.guns;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.Gun;
import dev.emortal.minestom.lazertag.gun.GunItemInfo;
import dev.emortal.minestom.lazertag.gun.ItemRarity;
import dev.emortal.minestom.lazertag.util.entity.BetterEntity;
import dev.emortal.minestom.lazertag.util.entity.BetterEntityProjectile;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.time.Duration;

public class BeeBlaster extends Gun {
    public BeeBlaster(LazerTagGame game) {
        super(
                game,
                "Bee Blaster",

                new GunItemInfo(
                        Material.HONEYCOMB,
                        ItemRarity.LEGENDARY,

                        70f,
                        0,
                        0,
                        1,

                        2000,
                        2000,
                        0,
                        0,
                        1,

                        Sound.sound(SoundEvent.ENTITY_BLAZE_HURT, Sound.Source.PLAYER, 1.3f, 1.1f)
                )
        );
    }

    @Override
    public void shoot(Player shooter) {
        BetterEntityProjectile entity = new BetterEntityProjectile(shooter, EntityType.BEE, () -> {
            // TODO: Some stuff here
        });
        entity.setDrag(false);
        entity.setGravityDrag(false);
        entity.setNoGravity(true);
        entity.setVelocity(shooter.getPosition().direction().mul(25));

        entity.scheduleRemove(Duration.ofSeconds(5));

        entity.setInstance(game.getInstance(), shooter.getPosition().add(0, shooter.getEyeHeight(), 0));
    }
}

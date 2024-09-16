package dev.emortal.minestom.lazertag.gun.guns;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.Gun;
import dev.emortal.minestom.lazertag.gun.GunItemInfo;
import dev.emortal.minestom.lazertag.gun.ItemRarity;
import dev.emortal.minestom.lazertag.util.entity.BetterEntityProjectile;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public final class BeeMinigun extends Gun {
    private static final GunItemInfo INFO = new GunItemInfo(
            Material.NETHERITE_SHOVEL,
            ItemRarity.RARE,

            1.1f,
            0,
            3,
            75,

            3500,
            100,
            50,
            0,
            1,

            Sound.sound(SoundEvent.ENTITY_BEE_HURT, Sound.Source.PLAYER, 1f, 2f)
    );

    public BeeMinigun(@NotNull LazerTagGame game) {
        super(game, "Bee Minigun", INFO);
    }

    @Override
    public void shoot(@NotNull Player shooter, int ammo) {
        BeeBlasterEntity entity = new BeeBlasterEntity(shooter);

        Pos spawnPos = shooter.getPosition().add(0, shooter.getEyeHeight() - EntityType.BEE.height() / 4, 0)
                .add(shooter.getPosition().direction().mul(1));

        entity.setInstance(this.game.getInstance(), spawnPos);
    }

    private final class BeeBlasterEntity extends BetterEntityProjectile {

        BeeBlasterEntity(@NotNull Player shooter) {
            super(shooter, EntityType.BEE);
            super.setAerodynamics(new Aerodynamics(0.0, 1.0, 0.0));
            super.setNoGravity(true);
            super.scheduleRemove(Duration.ofSeconds(3));

            BeeMeta meta = (BeeMeta) super.entityMeta;
            meta.setBaby(true);

            Vec shootDir = spread(shooter.getPosition().direction(), INFO.spread());
            super.setVelocity(shootDir.mul(50));
        }

        @Override
        public void collideBlock(@NotNull Point pos) {
            this.collide(null);
        }

        @Override
        public void collidePlayer(@NotNull Point pos, @NotNull Player player) {
            this.collide(player);
        }

        private void collide(@Nullable Player collidedPlayer) {
            this.getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_DONKEY_CHEST, Sound.Source.MASTER, 1f, 2f));

            this.remove();
            if (collidedPlayer == null) return;

            BeeMinigun.this.game.getDamageHandler().damage(collidedPlayer, this.shooter, this.getPosition(), INFO.damage());
        }
    }
}

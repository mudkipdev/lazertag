package dev.emortal.minestom.lazertag.gun.guns;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.Gun;
import dev.emortal.minestom.lazertag.gun.GunItemInfo;
import dev.emortal.minestom.lazertag.gun.ItemRarity;
import dev.emortal.minestom.lazertag.util.entity.BetterEntityProjectile;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockChucker extends Gun {
    private static final GunItemInfo INFO = new GunItemInfo(
            Material.ANVIL,
            ItemRarity.LEGENDARY,

            10f,
            0,
            0,
            5,

            3000,
            500,
            0,
            0,
            1,

            Sound.sound(SoundEvent.BLOCK_BONE_BLOCK_BREAK, Sound.Source.PLAYER, 2f, 0.5f)
    );

    public BlockChucker(@NotNull LazerTagGame game) {
        super(game, "Block Chucker", INFO);

        BlockChuckerEntity.initBlocks();
    }

    @Override
    public void shoot(@NotNull Player shooter, int ammo) {
        BlockChuckerEntity entity = new BlockChuckerEntity(shooter);

        Pos spawnPos = shooter.getPosition().add(0, shooter.getEyeHeight() - EntityType.BEE.height() / 2, 0)
                .add(shooter.getPosition().direction().mul(1));

        entity.setInstance(this.game.getInstance(), spawnPos);
    }

    private final class BlockChuckerEntity extends BetterEntityProjectile {

        private static final List<Block> BLOCKS;

        static {
            List<Block> blocks = new ArrayList<>();

            for (Block value : Block.values()) {
                if (!value.isSolid()) continue;

                Shape shape = value.registry().collisionShape();
                if (!shape.relativeStart().samePoint(0, 0, 0) || !shape.relativeEnd().samePoint(1, 1, 1)) continue;

                if (value.isAir()) continue;
                if (value.compare(Block.BARRIER)) continue;

                blocks.add(value);
            }

            BLOCKS = List.copyOf(blocks);
        }

        static void initBlocks() {
            // Will initialize the class and so initialize the blocks
        }

        public BlockChuckerEntity(@NotNull Player shooter) {
            super(shooter, EntityType.FALLING_BLOCK);
            ((FallingBlockMeta) super.entityMeta).setBlock(getRandomBlock());
            super.setAerodynamics(new Aerodynamics(0.0, 1.0, 0.0));
            super.setVelocity(shooter.getPosition().direction().mul(35));
            super.scheduleRemove(Duration.ofSeconds(5));
        }

        private static @NotNull Block getRandomBlock() {
            int randomIndex = ThreadLocalRandom.current().nextInt(BLOCKS.size());
            return BLOCKS.get(randomIndex);
        }

        @Override
        public void collideBlock(@NotNull Point pos) {
            this.collide();
        }

        @Override
        public void collidePlayer(@NotNull Point pos, @NotNull Player player) {
            this.collide();
        }

        private void collide() {
            Pos pos = this.getPosition();
            ServerPacket explosionPacket = new ExplosionPacket(pos.x(), pos.y(), pos.z(), 2f, new byte[0], 0f, 0f, 0);
            this.sendPacketToViewers(explosionPacket);

            for (Player victim : BlockChucker.this.game.getInstance().getPlayers()) {
                if (victim.isInvulnerable()) continue;
                if (victim.getDistanceSquared(this) > 5 * 5) continue;

                float damage = victim == this.shooter ? 5f : INFO.damage() / ((float) victim.getDistance(this) * 1.2f);
                BlockChucker.this.game.getDamageHandler().damage(victim, this.shooter, this.getPosition(), damage);

                victim.setVelocity(Vec.fromPoint(victim.getPosition().sub(this.getPosition().sub(0, 0.5, 0))).normalize().mul(10));
            }

            this.remove();
        }
    }
}

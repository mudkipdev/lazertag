package dev.emortal.minestom.lazertag.game;

import dev.emortal.minestom.lazertag.LazerTagModule;
import dev.emortal.minestom.lazertag.util.entity.BetterEntity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.position.PositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static net.kyori.adventure.title.Title.DEFAULT_TIMES;

public class DamageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DamageHandler.class);
    private static final Title YOU_DIED_TITLE = Title.title(
            Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
            Component.empty(),
            DEFAULT_TIMES
    );
    public static final Tag<Integer> KILLS_TAG = Tag.Integer("kills");
    public static final Tag<Long> SPAWN_PROT_TAG = Tag.Long("spawnProt");

    private static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("0.##");

    LazerTagGame game;
    public DamageHandler(LazerTagGame game) {
        this.game = game;
    }

    public void registerListeners(EventNode<InstanceEvent> eventNode) {
        eventNode.addListener(PlayerMoveEvent.class, e -> {
            if (e.getNewPosition().blockY() < 0) {
                kill(e.getPlayer());
            }
        });
    }


    public void damage(Player target, Player damager, Pos sourcePos, float damage) {
        if (target.getGameMode() != GameMode.ADVENTURE) return;

        setSpawnProtection(damager, 0L);
        if (hasSpawnProtection(target)) return;

        System.out.println("Health: " + target.getHealth());
        System.out.println("Damage: " + damage);
        if (getWouldDie(target, damage)) {
            kill(target);
            return;
        }

        Vec direction = Vec.fromPoint(sourcePos.sub(target.getPosition())).normalize();
        float yaw = PositionUtils.getLookYaw(direction.x(), direction.z());

//        game.getInstance().sendGroupedPacket(new DamageEventPacket(target.getEntityId(), 0, damager.getEntityId(), damager.getEntityId(), sourcePos));
        game.getInstance().sendGroupedPacket(new HitAnimationPacket(target.getEntityId(), yaw));

        spawnDamageIndicator(target.getPosition(), damage);

        target.damage(DamageType.fromPlayer(damager), damage);
    }

    private void spawnDamageIndicator(Pos playerPos, float damage) {
        BetterEntity entity = new BetterEntity(EntityType.TEXT_DISPLAY);
        entity.setDrag(false);
        entity.setGravityDrag(false);

        float healthPercentage = damage / 20f;

        TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
        meta.setText(Component.text("❤ " + HEALTH_FORMAT.format(damage), NamedTextColor.RED));
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setScale(new Vec(2, 2, 0).mul(healthPercentage).add(1, 1, 1));

        var rand = ThreadLocalRandom.current();

        entity.setVelocity(new Vec(
                rand.nextDouble(-1, 1),
                rand.nextDouble(4, 5),
                rand.nextDouble(-1, 1)
        ).mul(2)); // TODO: Make sure this is TPS independant

//        Pos newPos = playerPos.add(rand.nextDouble(-1.5, 1.5), rand.nextDouble(1.7, 2.2), rand.nextDouble(-1.5, 1.5));
        Pos newPos = playerPos.add(0, 1.7, 0);

        entity.scheduleRemove(1, ChronoUnit.SECONDS);
        entity.scheduler().buildTask(() -> {
            if (entity.isOnGround()) entity.remove();
        }).repeat(TaskSchedule.nextTick()).schedule();

        entity.setInstance(game.getInstance(), newPos);
    }

    public void kill(Player player) {
        if (player.getGameMode() != GameMode.ADVENTURE) return; // Already dead

        // TODO: calculate killer

        player.setAutoViewable(false);
        player.setGameMode(GameMode.SPECTATOR);
        player.showTitle(YOU_DIED_TITLE);
        player.heal();

        player.scheduler().buildTask(() -> {
            startRespawnTimer(player);
        }).delay(TaskSchedule.seconds(2)).schedule();
    }

    public void startRespawnTimer(Player player) {
        // Respawn task
        player.scheduler().submitTask(new Supplier<>() {
            int secondsLeft = 4;

            @Override
            public TaskSchedule get() {
                secondsLeft--;

                player.playSound(
                        Sound.sound(SoundEvent.BLOCK_WOODEN_BUTTON_CLICK_ON, Sound.Source.BLOCK, 1f, 1f),
                        Sound.Emitter.self()
                );
                player.showTitle(
                        Title.title(
                                Component.text(secondsLeft, NamedTextColor.GOLD, TextDecoration.BOLD),
                                Component.empty(),
                                Title.Times.times(
                                        Duration.ZERO, Duration.ofSeconds(1), Duration.ofMillis(200)
                                )
                        )
                );

                if (secondsLeft == 0) {
                    respawn(player);
                    return TaskSchedule.stop();
                }

                return TaskSchedule.seconds(1);
            }
        });
    }

    public void respawn(Player player) {
        player.teleport(getRandomSpawnPoint()).thenRun(() -> {
            player.playSound(
                    Sound.sound(SoundEvent.BLOCK_BEACON_ACTIVATE, Sound.Source.MASTER, 1f, 2f),
                    Sound.Emitter.self()
            );

            player.setAutoViewable(true);
            player.setGameMode(GameMode.ADVENTURE);
            player.clearTitle();
            giveRandomGun(player);
        });
    }

    private void giveRandomGun(Player player) {
        player.setItemInMainHand(game.getGunManager().getRandomGun().createItem());
    }

    private Pos getRandomSpawnPoint() {
        var rand = ThreadLocalRandom.current();

        String mapName = game.getCreationInfo().mapId();
        if (mapName == null) {
            LOGGER.warn("Map id was null, defaulting to 'dizzymc'!");
            mapName = "dizzymc";
        }
        Pos[] spawns = LazerTagModule.MAP_CONFIG_MAP.get(mapName).spawns;

        return spawns[rand.nextInt(spawns.length)];
    }

    private boolean getWouldDie(Player player, float damage) {
        return player.getHealth() <= damage;
    }

    public void setSpawnProtection(Player player, long millis) {
        if (millis == 0) {
            player.removeTag(SPAWN_PROT_TAG);
            return;
        }
        player.setTag(SPAWN_PROT_TAG, System.currentTimeMillis() + millis);
    }

    public static boolean hasSpawnProtection(Player player) {
        Long spawnProtMillis = player.getTag(SPAWN_PROT_TAG);
        if (spawnProtMillis == null) return false;

        return System.currentTimeMillis() > spawnProtMillis;
    }

}

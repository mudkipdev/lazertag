package dev.emortal.minestom.lazertag.game;

import dev.emortal.minestom.lazertag.LazerTagModule;
import dev.emortal.minestom.lazertag.util.entity.BetterEntity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.MathUtils;
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
    public static final Tag<Long> SPAWN_PROT_TAG = Tag.Long("spawnProt");

    private static final long COMBO_MILLIS_DEFAULT = 4000;
    public static final Tag<Long> COMBO_MILLIS_TAG = Tag.Long("comboMillis");
    public static final Tag<Integer> COMBO_TAG = Tag.Integer("combo");
    public static final Tag<Integer> KILLS_TAG = Tag.Integer("kills");
    public static final Tag<Integer> DEATHS_TAG = Tag.Integer("deaths");

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
        TextColor color = TextColor.lerp(healthPercentage, NamedTextColor.DARK_RED, NamedTextColor.GOLD);
        TextColor lighterColor = TextColor.color(
                MathUtils.clamp(color.red() + 100, 0, 255),
                MathUtils.clamp(color.green() + 100, 0, 255),
                MathUtils.clamp(color.blue() + 100, 0, 255)
        );

        TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();

        meta.setNotifyAboutChanges(false);
        meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
        meta.setScale(new Vec(2, 2, 0).mul(healthPercentage).add(1, 1, 1));
        meta.setShadow(true);
        meta.setText(
                Component.text()
                    .append(Component.text("❤ ", color))
                    .append(Component.text(HEALTH_FORMAT.format(damage), lighterColor))
                    .build()
        );
        meta.setNotifyAboutChanges(true);

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

        if (
                player.getLastDamageSource() instanceof EntityDamage entityDamage
                && entityDamage.getSource() instanceof Player killer
        ) {
            game.getInstance().sendMessage(getDeathMessage(player, killer));

            killer.showTitle(Title.title(
                    Component.empty(),
                    Component.text("☠" + player.getUsername(), NamedTextColor.RED),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ofMillis(700))
            ));

            int combo = getCombo(killer);
            killer.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 2f, 1f + (combo * 0.1f)), Sound.Emitter.self());
            incrementCombo(killer);
        } else {
            game.getInstance().sendMessage(getDeathMessage(player));
        }

        if (player instanceof FakePlayer) { // For testing!
            player.setGameMode(GameMode.SPECTATOR);
            player.scheduler().buildTask(() -> {
                player.setGameMode(GameMode.ADVENTURE);
            }).delay(TaskSchedule.tick(5)).schedule();
            player.heal();
            return;
        }

        // TODO: calculate killer and show them kill notification

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
    public boolean hasSpawnProtection(Player player) {
        Long spawnProtMillis = player.getTag(SPAWN_PROT_TAG);
        if (spawnProtMillis == null) return false;

        return System.currentTimeMillis() > spawnProtMillis;
    }

    private int getCombo(Player player) {
        long comboMillis = player.getTag(COMBO_MILLIS_TAG);
        if (comboMillis < System.currentTimeMillis()) { // Time has passed for combo
            player.setTag(COMBO_TAG, 0);
            return 0;
        }
        return player.getTag(COMBO_TAG);
    }
    private void incrementCombo(Player player) {
        int currentCombo = player.getTag(COMBO_TAG);
        player.setTag(COMBO_TAG, currentCombo + 1);
        player.setTag(COMBO_MILLIS_TAG, System.currentTimeMillis() + COMBO_MILLIS_DEFAULT);
    }

    public Component getDeathMessage(Player player) {
        return Component.text()
                .append(Component.text("☠", NamedTextColor.RED))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(player.getUsername(), NamedTextColor.RED))
                .append(Component.text(" died", NamedTextColor.GRAY))
                .build();
    }
    public Component getDeathMessage(Player player, Player killer) {
        return Component.text()
                .append(Component.text("☠", NamedTextColor.RED))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(killer.getUsername(), NamedTextColor.WHITE))
                .append(Component.text(" killed ", NamedTextColor.GRAY))
                .append(Component.text(player.getUsername(), NamedTextColor.RED))
//                .append(Component.text(" with ", NamedTextColor.GRAY))
//                .append(Component.text(gunName, NamedTextColor.GOLD))
                .build();
    }

}

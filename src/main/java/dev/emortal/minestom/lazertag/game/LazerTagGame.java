package dev.emortal.minestom.lazertag.game;

import dev.emortal.api.kurushimi.KurushimiMinestomUtils;
import dev.emortal.minestom.gamesdk.GameSdkModule;
import dev.emortal.minestom.gamesdk.config.GameCreationInfo;
import dev.emortal.minestom.gamesdk.game.Game;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public final class LazerTagGame extends Game {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final int MINIMUM_PLAYERS = 2;




    private final Instance instance;
    private final GameCreationInfo creationInfo;
    public LazerTagGame(final @NotNull GameCreationInfo creationInfo, @NotNull EventNode<Event> gameEventNode, @NotNull Instance instance) {
        super(creationInfo, gameEventNode);

        this.creationInfo = creationInfo;

        instance.setTimeRate(0);
        instance.setTimeUpdate(null);
        this.instance = instance;

        gameEventNode.addListener(PlayerDisconnectEvent.class, event -> {
            if (this.getPlayers().remove(event.getPlayer())) {
                if (this.getPlayers().size() <= 1) {
                    victory();
                }
            }
        });
    }

    public Instance getInstance() {
        return instance;
    }

    @Override
    public void onPlayerLogin(@NotNull PlayerLoginEvent playerLoginEvent) {

    }

    @Override
    public void start() {

    }

    @Override
    public void cancel() {

    }

    private void victory() {
//        if (gameTimerTask != null) gameTimerTask.cancel();

        Title victoryTitle = Title.title(
                MINI_MESSAGE.deserialize("<gradient:#ffc570:gold><bold>VICTORY!"),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(3))
        );
        Title defeatTitle = Title.title(
                MINI_MESSAGE.deserialize("<gradient:#ff474e:#ff0d0d><bold>DEFEAT!"),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(3))
        );

        Sound defeatSound = Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 1f, 1f);
        Sound victorySound = Sound.sound(SoundEvent.BLOCK_BEACON_POWER_SELECT, Sound.Source.MASTER, 1f, 0.8f);

        // Choose the player with the highest kills
        int killsRecord = 0;
        Player highestKiller = null;
        for (Player player : players) {
            Integer playerKills = player.getTag(DamageHandler.KILLS_TAG);
            if (playerKills == null) playerKills = 0;
            if (playerKills > killsRecord) {
                killsRecord = playerKills;
                highestKiller = player;
            }
        }

        for (Player player : players) {
            if (highestKiller == player) {
                player.showTitle(victoryTitle);
                player.playSound(victorySound, Sound.Emitter.self());
            } else {
                player.showTitle(defeatTitle);
                player.playSound(defeatSound, Sound.Emitter.self());
            }
        }

        instance.scheduler().buildTask(this::sendBackToLobby)
                .delay(TaskSchedule.seconds(6))
                .schedule();
    }

    private void sendBackToLobby() {
        for (final Player player : players) {
            player.setTeam(null);
            player.clearEffects();
        }
        KurushimiMinestomUtils.sendToLobby(players, this::removeGame, this::removeGame);
    }

    private void removeGame() {
        GameSdkModule.getGameManager().removeGame(this);
        cleanUp();
    }

    private void cleanUp() {
        for (final Player player : this.players) {
            player.kick(Component.text("The game ended but we weren't able to connect you to a lobby. Please reconnect", NamedTextColor.RED));
        }
        MinecraftServer.getInstanceManager().unregisterInstance(this.instance);
    }
}

package dev.emortal.minestom.lazertag.game;

import dev.emortal.minestom.lazertag.LazerTagModule;
import dev.emortal.minestom.lazertag.util.entity.BetterEntity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import net.minestom.server.scoreboard.Sidebar;
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
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScoreboardHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreboardHandler.class);

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    LazerTagGame game;
    Sidebar sidebar;
    public ScoreboardHandler(LazerTagGame game) {
        this.game = game;
        this.sidebar = new Sidebar(Component.text("☠", NamedTextColor.RED));

        int lines = 8;
        for (int i = 0; i < lines; i++) {
            sidebar.createLine(new Sidebar.ScoreboardLine(
                "kills" + i,
                    Component.empty(),
                    lines - i
            ));
        }

        sidebar.updateLineContent("kills0", Component.text("Be the first to kill!", NamedTextColor.GOLD));
    }

    public void registerListeners(EventNode<InstanceEvent> eventNode) {

    }

    public void showSidebar(Player player) {
        sidebar.addViewer(player);
    }
    public void hideSidebar(Player player) {
        sidebar.removeViewer(player);
    }

    public void refreshScoreboard() {
        game.getPlayers().stream().sorted(Comparator.comparingInt(a -> {
            Integer kills = a.getTag(DamageHandler.KILLS_TAG);
            if (kills == null) return 0;
            return kills;
        })).forEachOrdered(new Consumer<>() {
            int i = 0;

            @Override
            public void accept(Player player) {
                Integer kills = player.getTag(DamageHandler.KILLS_TAG);
                if (kills == null) return;

                Style color = switch (i + 1) {
                    case 1 -> Style.style(NamedTextColor.GOLD, TextDecoration.BOLD);
                    case 2 -> Style.style(TextColor.color(210, 210, 210), TextDecoration.BOLD);
                    case 3 -> Style.style(TextColor.color(205, 127, 50), TextDecoration.BOLD);
                    default -> Style.style(TextColor.color(140, 140, 140));
                };
                Style nameColor = switch (i + 1) {
                    case 1 -> Style.style(NamedTextColor.GOLD);
                    case 2 -> Style.style(TextColor.color(210, 210, 210));
                    case 3 -> Style.style(TextColor.color(205, 127, 50));
                    default -> Style.style(TextColor.color(140, 140, 140));
                };
                Style scoreColor = switch (i + 1) {
                    case 1,2,3 -> Style.style(NamedTextColor.LIGHT_PURPLE);
                    default -> Style.style(TextColor.fromHexString("#006c96"));
                };

                sidebar.updateLineContent(
                        "kills" + i,
                        Component.text()
                                .append(Component.text(i + 1 + ". ", color))
                                .append(Component.text(player.getUsername(), nameColor))
                                .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(kills, scoreColor))
                                .build()
                );

                i++;
            }
        });
    }

}

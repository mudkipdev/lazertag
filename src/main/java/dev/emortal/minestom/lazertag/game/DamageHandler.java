package dev.emortal.minestom.lazertag.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.tag.Tag;

import static net.kyori.adventure.title.Title.DEFAULT_TIMES;

public class DamageHandler {

    private static final Title YOU_DIED_TITLE = Title.title(
            Component.text("YOU DIED", NamedTextColor.RED, TextDecoration.BOLD),
            Component.empty(),
            DEFAULT_TIMES
    );
    public static final Tag<Integer> KILLS_TAG = Tag.Integer("kills");
    public static final Tag<Long> SPAWN_PROT_TAG = Tag.Long("spawnProt");

    LazerTagGame game;
    public DamageHandler(LazerTagGame game) {
        this.game = game;
    }

    public void registerListeners(EventNode<InstanceEvent> eventNode) {

    }


    public static void damage(Player target, Player damager, float damage) {
        if (target.getGameMode() != GameMode.ADVENTURE) return;

        setSpawnProtection(damager, 0L);
        if (hasSpawnProtection(target)) return;

        if (getWouldDie(target, damage)) {
            kill(target);
            return;
        }

        target.damage(DamageType.fromPlayer(damager), damage);
    }

    public static void kill(Player player) {
        // TODO: calculate killer

        player.setAutoViewable(false);
        player.setGameMode(GameMode.SPECTATOR);
        player.showTitle(YOU_DIED_TITLE);
        player.heal();
    }

    private static boolean getWouldDie(Player player, float damage) {
        return player.getHealth() < damage;
    }

    public static void setSpawnProtection(Player player, long millis) {
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

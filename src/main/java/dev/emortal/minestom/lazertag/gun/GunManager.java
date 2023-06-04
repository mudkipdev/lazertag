package dev.emortal.minestom.lazertag.gun;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.guns.LazerMinigun;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.Nullable;

public class GunManager {

    private final GunRegistry registry;
    private final LazerTagGame game;
    public GunManager(LazerTagGame game) {
        this.game = game;
        this.registry = new GunRegistry();
    }

    public void registerListeners(EventNode<InstanceEvent> eventNode) {
        eventNode.addListener(PlayerUseItemEvent.class, e -> {

        });
    }

    public Gun getRandomGun() {
        return registry.getRandomGun();
    }

    public void registerGuns() {
        registry.register(new LazerMinigun(game));
    }

    public @Nullable Gun getHeldPowerUp(Player player) {
        final ItemStack heldItem = player.getItemInMainHand();
        final String powerUpId = getPowerUpName(heldItem);
        return registry.getByName(powerUpId);
    }

    private String getPowerUpName(TagReadable powerUp) {
        return powerUp.getTag(Gun.NAME_TAG);
    }

}

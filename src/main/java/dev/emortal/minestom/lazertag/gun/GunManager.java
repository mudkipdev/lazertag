package dev.emortal.minestom.lazertag.gun;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class GunManager {

    private final GunRegistry registry;
    private final LazerTagGame game;
    public GunManager(LazerTagGame game) {
        this.game = game;
        this.registry = new GunRegistry(game);
    }

    public void registerListeners(EventNode<InstanceEvent> eventNode) {
        eventNode.addListener(InventoryPreClickEvent.class, e -> {
            e.setCancelled(true);
        });
        eventNode.addListener(PlayerChangeHeldSlotEvent.class, e -> {
            e.setCancelled(true);
            e.getPlayer().setHeldItemSlot((byte) 4);
        });

        eventNode.addListener(PlayerSwapItemEvent.class, e -> {
            // Reload
            e.setCancelled(true);
            reloadListener(e);
        });
        eventNode.addListener(ItemDropEvent.class, e -> {
            // Reload
            e.setCancelled(true);
            reloadListener(e);
        });

        eventNode.addListener(PlayerTickEvent.class, e -> {
            if (e.getPlayer().getAliveTicks() % (20 * 2) == 0) { // Only render ammo every 3 seconds
                Gun heldGun = getHeldPowerUp(e.getPlayer());
                if (heldGun == null) return;

                int ammo = e.getPlayer().getItemInMainHand().meta().getTag(Gun.AMMO_TAG);
                boolean reloading = e.getPlayer().getItemInMainHand().meta().hasTag(Gun.RELOADING_TAG);
                if (reloading) return;

                float ammoPercentage = ammo / (float) heldGun.getItemInfo().ammo();
                heldGun.renderAmmo(e.getPlayer(), ammoPercentage, false);
            }
        });

        eventNode.addListener(PlayerUseItemEvent.class, e -> {
            // Shoot
            e.setCancelled(true);

            // TODO: Gun cooldown

            ItemMeta itemMeta = e.getPlayer().getItemInMainHand().meta();
            if (itemMeta.hasTag(Gun.RELOADING_TAG)) return;
            if (itemMeta.getTag(Gun.COOLDOWN_TAG) > System.currentTimeMillis()) return;

            Gun heldGun = getHeldPowerUp(e.getPlayer());
            if (heldGun == null) return;

            int burstAmount = heldGun.getItemInfo().burstAmount();
            int burstDelayTicks = (int) heldGun.getItemInfo().burstDelay() / MinecraftServer.TICK_MS;

            heldGun.burstTaskMap.put(e.getPlayer().getUuid(), e.getPlayer().scheduler().submitTask(new Supplier<>() {
                int i = 0;

                @Override
                public TaskSchedule get() {
                    heldGun.shoot(e.getPlayer());
                    e.getInstance().playSound(heldGun.getItemInfo().sound(), e.getPlayer().getPosition());

                    i++;
                    if (i >= burstAmount) {
                        return TaskSchedule.stop();
                    }

                    return TaskSchedule.tick(burstDelayTicks);
                }
            }));
        });
    }

    private void reloadListener(PlayerInstanceEvent e) {
        Gun heldGun = getHeldPowerUp(e.getPlayer());
        if (heldGun == null) return;

        ItemMeta itemMeta = e.getPlayer().getItemInMainHand().meta();
        if (itemMeta.hasTag(Gun.RELOADING_TAG)) return;
        if (itemMeta.getTag(Gun.AMMO_TAG) == heldGun.getItemInfo().ammo()) return;

        heldGun.reload(e.getPlayer());
    }

    public Gun getRandomGun() {
        return registry.getRandomGun();
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

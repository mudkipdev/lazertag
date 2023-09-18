package dev.emortal.minestom.lazertag.gun;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class GunManager {

    private final @NotNull LazerTagGame game;
    private final @NotNull GunRegistry registry;

    public GunManager(@NotNull LazerTagGame game) {
        this.game = game;
        this.registry = new GunRegistry(game);
    }

    public void registerListeners() {
        this.game.getEventNode().addListener(InventoryPreClickEvent.class, event -> event.setCancelled(true));
        this.game.getEventNode().addListener(PlayerChangeHeldSlotEvent.class, event -> {
            event.setCancelled(true);
            event.getPlayer().setHeldItemSlot((byte) 4);
        });

        this.game.getEventNode().addListener(PlayerSwapItemEvent.class, event -> {
            // Reload
            event.setCancelled(true);
            this.onReload(event);
        });
        this.game.getEventNode().addListener(ItemDropEvent.class, event -> {
            // Reload
            event.setCancelled(true);
            this.onReload(event);
        });

        this.game.getEventNode().addListener(PlayerTickEvent.class, this::onTick);
        this.game.getEventNode().addListener(PlayerUseItemEvent.class, this::onItemUse);
    }

    private void onReload(@NotNull PlayerInstanceEvent event) {
        Player player = event.getPlayer();

        Gun heldGun = getHeldPowerUp(player);
        if (heldGun == null) return;

        ItemMeta itemMeta = player.getItemInMainHand().meta();
        if (itemMeta.hasTag(Gun.RELOADING_TAG)) return;
        if (itemMeta.getTag(Gun.AMMO_TAG) == heldGun.getItemInfo().ammo()) return;

        heldGun.reload(player);
    }

    private void onTick(@NotNull PlayerTickEvent event) {
        Player player = event.getPlayer();
        if (player.getAliveTicks() % (20 * 2) != 0) return; // Only render ammo every 3 seconds

        Gun heldGun = this.getHeldPowerUp(player);
        if (heldGun == null) return;

        int ammo = player.getItemInMainHand().meta().getTag(Gun.AMMO_TAG);
        boolean reloading = player.getItemInMainHand().meta().hasTag(Gun.RELOADING_TAG);
        if (reloading) return;

        float ammoPercentage = ammo / (float) heldGun.getItemInfo().ammo();
        heldGun.renderAmmo(player, ammoPercentage, false);
    }

    private void onItemUse(@NotNull PlayerUseItemEvent event) {
        Player player = event.getPlayer();
        // Shoot
        event.setCancelled(true);

        // TODO: Gun cooldown

        ItemMeta itemMeta = player.getItemInMainHand().meta();
        if (itemMeta.hasTag(Gun.RELOADING_TAG)) return;
        if (itemMeta.getTag(Gun.COOLDOWN_TAG) > System.currentTimeMillis()) return;

        Gun heldGun = this.getHeldPowerUp(player);
        if (heldGun == null) return;

        int burstAmount = heldGun.getItemInfo().burstAmount();
        int burstDelayTicks = (int) heldGun.getItemInfo().burstDelay() / MinecraftServer.TICK_MS;

        player.scheduler().submitTask(new Supplier<>() {
            int i = 0;

            @Override
            public TaskSchedule get() {
                int ammo = player.getItemInMainHand().meta().getTag(Gun.AMMO_TAG) - 1;
                if (ammo < 0) return TaskSchedule.stop();

                heldGun.shoot(player, ammo);
                heldGun.afterShoot(player, ammo);
                event.getInstance().playSound(heldGun.getItemInfo().sound(), player.getPosition());

                this.i++;
                if (this.i >= burstAmount) {
                    return TaskSchedule.stop();
                }

                return TaskSchedule.tick(burstDelayTicks);
            }
        });
    }

    public @NotNull Gun getRandomGun() {
        return this.registry.getRandomGun();
    }

    public @Nullable Gun getHeldPowerUp(@NotNull Player player) {
        ItemStack heldItem = player.getItemInMainHand();
        String powerUpId = this.getPowerUpName(heldItem);
        return this.registry.getByName(powerUpId);
    }

    private @NotNull String getPowerUpName(@NotNull ItemStack powerUp) {
        return powerUp.getTag(Gun.NAME_TAG);
    }
}

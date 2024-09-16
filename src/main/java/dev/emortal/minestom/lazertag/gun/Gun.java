package dev.emortal.minestom.lazertag.gun;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.raycast.RaycastResult;
import dev.emortal.minestom.lazertag.raycast.RaycastUtil;
import dev.emortal.minestom.lazertag.util.ParticleUtil;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public abstract class Gun {
    private static final Component RELOADING_COMPONENT = Component.text("RELOADING ", NamedTextColor.RED);

    public static final @NotNull Tag<String> NAME_TAG = Tag.String("name");
    public static final @NotNull Tag<Integer> AMMO_TAG = Tag.Integer("ammo");
    public static final @NotNull Tag<Boolean> RELOADING_TAG = Tag.Boolean("reloading");
    public static final @NotNull Tag<Long> COOLDOWN_TAG = Tag.Long("cooldown");

    protected final @NotNull LazerTagGame game;
    private final @NotNull String name;
    private final @NotNull GunItemInfo itemInfo;

    protected Gun(@NotNull LazerTagGame game, @NotNull String name, @NotNull GunItemInfo itemInfo) {
        this.game = game;
        this.name = name;
        this.itemInfo = itemInfo;
    }

    public void shoot(@NotNull Player shooter, int ammo) {
        for (int i = 0; i < this.itemInfo.bullets(); i++) {
            Vec shootDir = spread(shooter.getPosition().direction(), this.itemInfo.spread());
            Pos eyePos = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);

            RaycastResult raycast = RaycastUtil.raycast(this.game.getInstance(), eyePos, shootDir, this.itemInfo.distance(),
                    entity -> entity != shooter && entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE);
            Point hitPoint = raycast.hitPosition() == null ? eyePos.add(shootDir.mul(this.itemInfo.distance())) : raycast.hitPosition();

            if (raycast.hitEntity() != null) { // Hit entity
                this.game.getDamageHandler().damage((Player) raycast.hitEntity(), shooter, shooter.getPosition(), this.itemInfo.damage());
            } else { // Hit block
                // TODO: hit block animation
            }

            ParticleUtil.renderBulletTrail(this.game.getInstance(), eyePos.add(shootDir.mul(2.0)), hitPoint, 1.5);
        }
    }

    public void afterShoot(Player shooter, int ammo) {
        float ammoPercentage = ammo / (float) this.itemInfo.ammo();
        this.renderAmmo(shooter, ammoPercentage, false);

        // Decrease ammo
        shooter.setItemInMainHand(shooter.getItemInMainHand()
                .withTag(AMMO_TAG, ammo)
                .withTag(COOLDOWN_TAG, System.currentTimeMillis() + this.itemInfo.shootDelay()));

        // If ran out of ammo - reload!
        if (ammo == 0) {
            this.reload(shooter);
        }
    }

    public void reload(@NotNull Player player) {
        ItemStack item = player.getItemInMainHand()
                .withTag(RELOADING_TAG, true)
                .withTag(AMMO_TAG, 0);

        player.setItemInMainHand(item);
        player.playSound(Sound.sound(SoundEvent.BLOCK_ANVIL_LAND, Sound.Source.PLAYER, 0.7f, 2f));
        player.scheduler().submitTask(new Supplier<>() {
            final long startingReloadTicks = Gun.this.itemInfo.reloadTime() / MinecraftServer.TICK_MS;

            long reloadTicks = this.startingReloadTicks;
            int lastAmmo = -1;
//            long currentAmmo = 0;

            @Override
            public TaskSchedule get() {
                this.reloadTicks--;

                if (this.reloadTicks == 0) {
                    // Fully reloaded!
                    Gun.this.playReloadSound(player);

                    player.setItemInMainHand(item
                            .withTag(RELOADING_TAG, null)
                            .withTag(AMMO_TAG, Gun.this.itemInfo.ammo()));

                    Gun.this.renderAmmo(player, 1f, false);
                    return TaskSchedule.stop();
                }

                float percentage = 1f - (this.reloadTicks / (float) this.startingReloadTicks);
                int ammo = (int) (Gun.this.itemInfo.ammo() * percentage);

                if (ammo != this.lastAmmo) {
                    this.lastAmmo = ammo;
                    player.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.MASTER, 0.2f, 1f), Sound.Emitter.self());
                }

                Gun.this.renderAmmo(player, percentage, true);
                return TaskSchedule.tick(1);
            }
        });
    }

    protected void playReloadSound(@NotNull Player player) {
        player.playSound(Sound.sound(SoundEvent.ENTITY_IRON_GOLEM_ATTACK, Sound.Source.PLAYER, 1f, 1f));
        player.scheduler()
                .buildTask(() -> player.playSound(Sound.sound(SoundEvent.ENTITY_IRON_GOLEM_ATTACK, Sound.Source.PLAYER, 1f, 1f)))
                .delay(TaskSchedule.millis(150))
                .schedule();
    }

    public void renderAmmo(@NotNull Player player, float percentage, boolean reloading) {
        TextComponent.Builder component = Component.text();
        if (reloading) {
            component.append(RELOADING_COMPONENT);
        }

        component.append(createProgressBar(percentage, 40, "|", reloading ? NamedTextColor.RED : NamedTextColor.GOLD, NamedTextColor.DARK_GRAY));

        int ammo = (int) (this.itemInfo.ammo() * percentage);

        component.append(Component.space());
        component.append(Component.text(String.format("%0" + String.valueOf(this.itemInfo.ammo()).length() + "d", ammo), NamedTextColor.WHITE));

        player.sendActionBar(component.build());
    }

    public static @NotNull Vec spread(@NotNull Vec vec, double amount) {
        if (amount == 0.0) return vec;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        return vec
                .rotateAroundX(random.nextDouble(-amount, amount))
                .rotateAroundY(random.nextDouble(-amount, amount))
                .rotateAroundZ(random.nextDouble(-amount, amount));
    }

    private static @NotNull Component createProgressBar(float percentage, int charLength, @NotNull String character, @NotNull RGBLike completeColor,
                                                        @NotNull RGBLike incompleteColor) {
        int completeCharacters = (int) Math.ceil(percentage * charLength);
        int incompleteCharacters = charLength - completeCharacters;

        return Component.text()
                .append(Component.text(character.repeat(completeCharacters), TextColor.color(completeColor)))
                .append(Component.text(character.repeat(incompleteCharacters), TextColor.color(incompleteColor)))
                .build();
    }

    public @NotNull ItemStack createItem() {
        return ItemStack.builder(this.itemInfo.material())
                .set(ItemComponent.ITEM_NAME, Component.text(this.name))
                .set(ItemComponent.LORE, List.of(this.itemInfo.rarity().getName().decoration(TextDecoration.ITALIC, false)))
                .set(AMMO_TAG, this.itemInfo.ammo())
                .set(NAME_TAG, this.name)
                .set(COOLDOWN_TAG, 0L)
                .build();
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull GunItemInfo getItemInfo() {
        return this.itemInfo;
    }
}

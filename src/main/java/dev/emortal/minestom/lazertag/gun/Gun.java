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
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public abstract class Gun {
    private static final Component RELOADING_COMPONENT = Component.text("RELOADING ", NamedTextColor.RED);

    public static final Tag<String> NAME_TAG = Tag.String("name");
    public static final Tag<Integer> AMMO_TAG = Tag.Integer("ammo");
    public static final Tag<Boolean> RELOADING_TAG = Tag.Boolean("reloading");
    public static final Tag<Long> COOLDOWN_TAG = Tag.Long("cooldown");

    protected final @NotNull LazerTagGame game;
    protected final @NotNull String name;
    private final @NotNull GunItemInfo itemInfo;

    protected Gun(@NotNull LazerTagGame game, @NotNull String name, @NotNull GunItemInfo itemInfo) {
        this.game = game;
        this.name = name;
        this.itemInfo = itemInfo;
    }

    public void shoot(@NotNull Player shooter) {
        int ammo = shooter.getItemInMainHand().meta().getTag(AMMO_TAG) - 1;
        if (ammo < 0) return;

        float ammoPercentage = ammo / (float) this.itemInfo.ammo();
        this.renderAmmo(shooter, ammoPercentage, false);

        for (int i = 0; i < this.itemInfo.bullets(); i++) {
            Vec shootDir = spread(shooter.getPosition().direction(), this.itemInfo.spread());
            Pos eyePos = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);

            RaycastResult raycast = RaycastUtil.raycast(this.game.getSpawningInstance(), eyePos, shootDir, this.itemInfo.distance(),
                    entity -> entity != shooter && entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE);
            Point hitPoint = raycast.hitPosition() == null ? eyePos.add(shootDir.mul(this.itemInfo.distance())) : raycast.hitPosition();

            if (raycast.hitEntity() != null) { // Hit entity
                this.game.getDamageHandler().damage((Player) raycast.hitEntity(), shooter, shooter.getPosition(), this.itemInfo.damage());
            } else { // Hit block
                // TODO: hit block animation
            }

            ParticleUtil.renderBulletTrail(this.game.getSpawningInstance(), eyePos.add(shootDir.mul(2.0)), hitPoint, 1.5);
        }

        // Decrease ammo
        shooter.setItemInMainHand(shooter.getItemInMainHand().withMeta(meta -> {
            meta.setTag(AMMO_TAG, ammo);
            meta.setTag(COOLDOWN_TAG, System.currentTimeMillis() + this.itemInfo.shootDelay());
        }));

        // If ran out of ammo - reload!
        if (ammo == 0) {
            this.reload(shooter);
        }
    }

    public void reload(@NotNull Player player) {
        ItemStack item = player.getItemInMainHand();
        player.setItemInMainHand(item.withMeta(meta -> {
            meta.setTag(RELOADING_TAG, true);
            meta.setTag(AMMO_TAG, 0);
        }));

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

                    player.setItemInMainHand(item.withMeta(meta -> {
                        meta.removeTag(RELOADING_TAG);
                        meta.setTag(AMMO_TAG, Gun.this.itemInfo.ammo());
                    }));
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
        player.scheduler().buildTask(() -> player.playSound(Sound.sound(SoundEvent.ENTITY_IRON_GOLEM_ATTACK, Sound.Source.PLAYER, 1f, 1f)))
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
                .meta(meta -> {
                    meta.displayName(Component.text(this.name).decoration(TextDecoration.ITALIC, false));
                    meta.lore(this.itemInfo.rarity().getName().decoration(TextDecoration.ITALIC, false));
                    meta.setTag(AMMO_TAG, this.itemInfo.ammo());
                    meta.setTag(NAME_TAG, this.name);
                    meta.setTag(COOLDOWN_TAG, 0L);
                })
                .build();
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull GunItemInfo getItemInfo() {
        return this.itemInfo;
    }
}

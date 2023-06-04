package dev.emortal.minestom.lazertag.gun;

import dev.emortal.minestom.lazertag.game.DamageHandler;
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
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class Gun {

    public static final Tag<String> NAME_TAG = Tag.String("name");
    public static final Tag<Integer> AMMO_TAG = Tag.Integer("ammo");
    public static final Tag<Boolean> RELOADING_TAG = Tag.Boolean("reloading");

    public static final Map<UUID, Task> RELOAD_TASK_MAP = new HashMap<>();


    protected final LazerTagGame game;
    protected final String name;
    private final GunItemInfo itemInfo;

    public Gun(LazerTagGame game, String name, GunItemInfo itemInfo) {
        this.game = game;
        this.name = name;
        this.itemInfo = itemInfo;
    }

    public void shoot(Player shooter) {

        int ammo = shooter.getItemInMainHand().meta().getTag(AMMO_TAG);
        float ammoPercentage = ammo / (float) itemInfo.ammo();
        renderAmmo(shooter, ammoPercentage, false);

        Vec eyeDir = shooter.getPosition().direction();
        Pos eyePos = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);

        RaycastResult raycast = RaycastUtil.raycast(game.getInstance(), eyePos, eyeDir, itemInfo.distance(), (entity) -> {
            return entity != shooter && entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE;
        });
        Point hitPoint = raycast.hitPosition() == null ? eyePos.add(eyeDir.mul(itemInfo.distance())) : raycast.hitPosition();

        ParticleUtil.renderBulletTrail(game.getInstance(), eyePos, hitPoint, 1.5);

        if (raycast.hitEntity() != null) { // Hit entity
            DamageHandler.damage((Player) raycast.hitEntity(), shooter, itemInfo.damage());
        } else { // Hit block
            // TODO: hit block animation
        }

        // If ran out of ammo - reload!
        if (ammo == 0) {
            reload(shooter);
        }
    }

    public void reload(Player player) {
        ItemStack item = player.getItemInMainHand();
        player.setItemInMainHand(item.withMeta(meta -> {
            meta.setTag(RELOADING_TAG, true);
            meta.setTag(AMMO_TAG, 0);
        }));

        RELOAD_TASK_MAP.put(player.getUuid(), player.scheduler().submitTask(new Supplier<>() {
            final long startingReloadTicks = itemInfo.reloadTime() / MinecraftServer.TICK_MS;
            long reloadTicks = startingReloadTicks;
//            long currentAmmo = 0;

            @Override
            public TaskSchedule get() {
                reloadTicks--;

                if (reloadTicks == 0) {
                    // Fully reloaded!
                    playReloadSound(player);

                    player.setItemInMainHand(item.withMeta(meta -> {
                        meta.removeTag(RELOADING_TAG);
                        meta.setTag(AMMO_TAG, itemInfo.ammo());
                    }));
                }

                float percentage = (1f - reloadTicks) / (float) startingReloadTicks;
                renderAmmo(player, percentage, true);

                player.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.MASTER, 0.2f, 1f), Sound.Emitter.self());

                return TaskSchedule.tick(1);
            }
        }));
    }

    private void playReloadSound(Player player) {
        player.playSound(Sound.sound(SoundEvent.ENTITY_IRON_GOLEM_ATTACK, Sound.Source.PLAYER, 1f, 1f));
        player.scheduler().buildTask(() -> {
            player.playSound(Sound.sound(SoundEvent.ENTITY_IRON_GOLEM_ATTACK, Sound.Source.PLAYER, 1f, 1f));
        }).delay(TaskSchedule.millis(150)).schedule();
    }


    private static final Component RELOADING_COMPONENT = Component.text("RELOADING ", NamedTextColor.RED);
    private void renderAmmo(Player player, float percentage, boolean reloading) {
        TextComponent.Builder component = Component.text();

        if (reloading) component.append(RELOADING_COMPONENT);

        component.append(
                createProgressBar(
                        percentage,
                        40,
                        "|",
                        reloading ? NamedTextColor.RED : NamedTextColor.GOLD,
                        NamedTextColor.DARK_GRAY
                )
        );

//        component.append(Component.space());
//        component.append(Component.text(String.format("%0" + String.valueOf(ammo).length() + "d", currentAmmo), NamedTextColor.DARK_GRAY));

        player.sendActionBar(component.build());
    }

    public static Vec spread(Vec vec, double amount) {
        return vec
                .rotateAroundX(amount)
                .rotateAroundY(amount)
                .rotateAroundZ(amount);
    }

    private static Component createProgressBar(float percentage, int charLength, String character, RGBLike completeColor, RGBLike incompleteColor) {
        int completeCharacters = (int) Math.ceil(percentage * charLength);
        int incompleteCharacters = charLength - completeCharacters;

        return Component.text(character.repeat(completeCharacters), TextColor.color(completeColor))
                .append(Component.text(character.repeat(incompleteCharacters), TextColor.color(completeColor)));
    }

    public ItemStack createItem() {
        return ItemStack.builder(itemInfo.material())
                .meta(meta -> {
                    meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
                    meta.lore(itemInfo.rarity().getName().decoration(TextDecoration.ITALIC, false));
                    meta.setTag(AMMO_TAG, itemInfo.ammo());
                    meta.setTag(NAME_TAG, name);
                })
                .build();
    }

    public LazerTagGame getGame() {
        return game;
    }

    public String getName() {
        return name;
    }

    public GunItemInfo getItemInfo() {
        return itemInfo;
    }
}

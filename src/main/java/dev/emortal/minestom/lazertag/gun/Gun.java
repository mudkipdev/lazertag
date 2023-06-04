package dev.emortal.minestom.lazertag.gun;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import kotlin.reflect.jvm.internal.impl.descriptors.Named;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public class Gun {

    private final float damage;
    private final double distance;
    private final float burstAmount;

    private final int ammo;

    private final long reloadTime;
    private final long shootDelay;
    private final long burstDelay;

    private final Sound sound;

    private final ItemStack item;


    private int currentAmmo;

    public Gun(
            float damage,
            double distance,
            float burstAmount,
            int ammo,

            long reloadTime,
            long shootDelay,
            long burstDelay,

            Sound sound,

            ItemStack item
    ) {
        this.damage = damage;
        this.distance = distance;
        this.burstAmount = burstAmount;
        this.ammo = ammo;

        this.reloadTime = reloadTime;
        this.shootDelay = shootDelay;
        this.burstDelay = burstDelay;

        this.sound = sound;

        this.item = item;
    }

    public void shoot(LazerTagGame game, Player shooter) {

    }

    public static Vec spread(Vec vec, double amount) {
        return vec
                .rotateAroundX(amount)
                .rotateAroundY(amount)
                .rotateAroundZ(amount);
    }


    private static final Component RELOADING_COMPONENT = Component.text("RELOADING ", NamedTextColor.RED);
    private void renderAmmo(Player player, int currentAmmo, boolean reloading) {
        TextComponent.Builder component = Component.text();

        if (reloading) component.append(RELOADING_COMPONENT);

        component.append(
                createProgressBar(
                        (float)currentAmmo / (float)this.ammo,
                        40,
                        "|",
                        reloading ? NamedTextColor.RED : NamedTextColor.GOLD,
                        NamedTextColor.DARK_GRAY
                )
        );

        component.append(Component.space());
        component.append(Component.text(String.format("%0" + String.valueOf(ammo).length() + "d", currentAmmo), NamedTextColor.DARK_GRAY));
    }
    private static Component createProgressBar(float percentage, int charLength, String character, RGBLike completeColor, RGBLike incompleteColor) {
        int completeCharacters = (int) Math.ceil(percentage * charLength);
        int incompleteCharacters = charLength - completeCharacters;

        return Component.text(character.repeat(completeCharacters), TextColor.color(completeColor))
                .append(Component.text(character.repeat(incompleteCharacters), TextColor.color(completeColor)));
    }

}

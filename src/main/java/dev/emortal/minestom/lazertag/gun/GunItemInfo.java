package dev.emortal.minestom.lazertag.gun;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public record GunItemInfo(
        @NotNull Material material, @NotNull ItemRarity rarity,
        float damage, double distance, int burstAmount, int ammo,
        long reloadTime, long shootDelay, long burstDelay,
        double spread, int bullets,
        Sound sound
) {


}
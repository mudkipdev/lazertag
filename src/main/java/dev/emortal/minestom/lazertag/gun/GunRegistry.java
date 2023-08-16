package dev.emortal.minestom.lazertag.gun;

import dev.emortal.minestom.lazertag.game.LazerTagGame;
import dev.emortal.minestom.lazertag.gun.guns.AssaultRifle;
import dev.emortal.minestom.lazertag.gun.guns.BeeBlaster;
import dev.emortal.minestom.lazertag.gun.guns.LazerMinigun;
import dev.emortal.minestom.lazertag.gun.guns.Shotgun;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GunRegistry {

    private final Map<String, Gun> registry = new HashMap<>();
    private final Gun[] guns;
    private final LazerTagGame game;

    public GunRegistry(LazerTagGame game) {
        this.game = game;

        registerGuns();
        guns = registry.values().toArray(new Gun[0]);
    }

    public @Nullable Gun getByName(final @NotNull String name) {
        return registry.get(name);
    }

    public @NotNull Gun getRandomGun() {
        int totalWeight = 0;
        for (Gun value : guns) {
            totalWeight += value.getItemInfo().rarity().getWeight();
        }

        int index = 0;
        int randomIndex = ThreadLocalRandom.current().nextInt(totalWeight + 1);
        while (index < guns.length - 1) {
            randomIndex -= guns[index].getItemInfo().rarity().getWeight();
            if (randomIndex <= 0) break;
            index++;
        }

        return guns[index];
    }

    public void register(@NotNull Gun powerUp) {
        final String name = powerUp.getName();
        if (registry.containsKey(name)) {
            throw new IllegalArgumentException("Power up with name " + name + " already exists!");
        }
        registry.put(name, powerUp);
    }

    public @NotNull Collection<String> getPowerUpNames() {
        return registry.keySet();
    }

    public void registerGuns() {
        register(new AssaultRifle(game));
        register(new BeeBlaster(game));
        register(new LazerMinigun(game));
        register(new Shotgun(game));
    }
}

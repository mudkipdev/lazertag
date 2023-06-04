package dev.emortal.minestom.lazertag.gun;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GunRegistry {
    private final Map<String, Gun> registry = new HashMap<>();

    public @Nullable Gun getByName(final @NotNull String name) {
        return registry.get(name);
    }

    public @NotNull Gun getRandomGun() {
        Gun[] guns = registry.values().toArray(new Gun[0]);

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
}

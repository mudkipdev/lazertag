package dev.emortal.minestom.lazertag.raycast;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * If hit entity and hit position are both null, nothing was hit
 * If hit entity is null but hit position isn't, a block was hit
 * If hit entity nor hit position are null, an entity was hit
 */
public record RaycastResult(@Nullable Entity hitEntity, @Nullable Point hitPosition) {
    public static final RaycastResult HIT_NOTHING = new RaycastResult(null, null);

}

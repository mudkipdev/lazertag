package dev.emortal.minestom.lazertag.raycast;

import dev.emortal.rayfast.area.area3d.Area3d;
import dev.emortal.rayfast.area.area3d.Area3dRectangularPrism;
import dev.emortal.rayfast.casting.grid.GridCast;
import dev.emortal.rayfast.vector.Vector3d;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

public final class RaycastUtil {
    private static final double TOLERANCE = 0.0;

    private static final Map<BoundingBox, Area3d> BOUNDING_BOX_TO_AREA_3D_MAP = new HashMap<>();

    public static void init() {
        Area3d.CONVERTER.register(BoundingBox.class, box -> {
            BOUNDING_BOX_TO_AREA_3D_MAP.computeIfAbsent(box, bb -> Area3dRectangularPrism.of(
                    bb.minX() - TOLERANCE, bb.minY() - TOLERANCE, bb.minZ() - TOLERANCE,
                    bb.maxX() + TOLERANCE, bb.maxY() + TOLERANCE, bb.maxZ() + TOLERANCE
            ));

            return BOUNDING_BOX_TO_AREA_3D_MAP.get(box);
        });
    }

    public static boolean hasLineOfSight(@NotNull Entity a, @NotNull Entity b) {
        return hasLineOfSight(
                a.getInstance(),
                a.getPosition().add(0, a.getEyeHeight(), 0),
                b.getPosition().add(0, b.getEyeHeight(), 0));
    }

    public static boolean hasLineOfSight(@NotNull Instance instance, @NotNull Point startPoint, @NotNull Point endPoint) {
        Vec direction = Vec.fromPoint(endPoint.sub(startPoint)).normalize();
        double maxDistance = startPoint.distance(endPoint);
        return raycastBlock(instance, startPoint, direction, maxDistance) == null;
    }

    public static @Nullable Point raycastBlock(@NotNull Instance instance, @NotNull Point startPoint, @NotNull Point direction, double maxDistance) {
        Iterator<Vector3d> gridIter = GridCast.createExactGridIterator(
                startPoint.x(), startPoint.y(), startPoint.z(),
                direction.x(), direction.y(), direction.z(),
                1.0, maxDistance
        );

        while (gridIter.hasNext()) {
            Vector3d gridUnit = gridIter.next();
            Point pos = new Vec(gridUnit.x(), gridUnit.y(), gridUnit.z());

            try {
                Block hitBlock = instance.getBlock(pos);

                if (hitBlock.isSolid()) {
                    return pos;
                }
            } catch (NullPointerException ignored) {
                // catches unloaded chunk errors
                break;
            }
        }

        return null;
    }

    public static @NotNull RaycastResult raycastEntity(@NotNull Instance instance, @NotNull Point startPoint, @NotNull Point direction,
                                                       double maxDistance, @NotNull Predicate<Entity> hitFilter) {
        for (Entity entity : instance.getEntities()) {
            if (!hitFilter.test(entity)) continue;
            if (entity.getPosition().distanceSquared(startPoint) > maxDistance * maxDistance) continue;

            Area3d area3d = Area3d.CONVERTER.from(entity.getBoundingBox());
            Pos entityPos = entity.getPosition();

            Vector3d intersection = area3d.lineIntersection(
                    startPoint.x() - entityPos.x(), startPoint.y() - entityPos.y(), startPoint.z() - entityPos.z(),
                    direction.x(), direction.y(), direction.z()
            );
            if (intersection != null) {
                return new RaycastResult(entity, new Vec(intersection.x() + entityPos.x(), intersection.y() + entityPos.y(), intersection.z() + entityPos.z()));
            }
        }

        return RaycastResult.HIT_NOTHING;
    }

    public static @NotNull RaycastResult raycast(@NotNull Instance instance, @NotNull Point startPoint, @NotNull Point direction, double maxDistance,
                                                 @NotNull Predicate<Entity> hitFilter) {
        Point blockRaycast = raycastBlock(instance, startPoint, direction, maxDistance);
        RaycastResult entityRaycast = raycastEntity(instance, startPoint, direction, maxDistance, hitFilter);

        if (entityRaycast == RaycastResult.HIT_NOTHING && blockRaycast == null) {
            return new RaycastResult(null, null);
        }

        // block raycast is always true when reached
        if (entityRaycast == RaycastResult.HIT_NOTHING) {
            return new RaycastResult(null, blockRaycast);
        }

        // entity raycast is always true when reached
        if (blockRaycast == null) {
            return entityRaycast;
        }

        // Both entity and block check have collided, time to see which is closer!

        double distanceToEntity = startPoint.distanceSquared(entityRaycast.hitPosition());
        double distanceToBlock = startPoint.distanceSquared(blockRaycast);

        if (distanceToBlock > distanceToEntity) {
            return entityRaycast;
        } else {
            return new RaycastResult(null, blockRaycast);
        }
    }

    private RaycastUtil() {
    }
}

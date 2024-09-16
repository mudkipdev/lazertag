package dev.emortal.minestom.lazertag.util;

import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public interface ParticleUtil {
    static void renderBulletTrail(@NotNull PacketGroupingAudience audience, @NotNull Point start, @NotNull Point end, double step) {
        Point current = Vec.fromPoint(start);
        double distRemaining = start.distanceSquared(end);

        Vec dir = Vec.fromPoint(end.sub(start)).normalize().mul(step);
        double dirLength = dir.lengthSquared();

        while (distRemaining > 0) {
            audience.sendGroupedPacket(new ParticlePacket(
                    Particle.DUST.withColor(new Color(255, 255, 0)),
                    true,
                    current,
                    Vec.ZERO,
                    0f,
                    1));

            distRemaining -= dirLength;
            current = current.add(dir);
        }
    }
}

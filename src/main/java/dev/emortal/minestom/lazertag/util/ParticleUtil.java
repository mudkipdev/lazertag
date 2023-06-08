package dev.emortal.minestom.lazertag.util;

import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;

public class ParticleUtil {

    public static void renderBulletTrail(PacketGroupingAudience audience, Point start, Point end, double step) {
        Point current = Vec.fromPoint(start);
        double distRemaining = start.distanceSquared(end);

        Vec dir = Vec.fromPoint(end.sub(start)).normalize().mul(step);
        double dirLength = dir.lengthSquared();

        while (distRemaining > 0) {
            ParticlePacket packet = ParticleCreator.createParticlePacket(Particle.DUST, true, current.x(), current.y(), current.z(), 0f, 0f, 0f, 0f, 1, (writer) -> {
                writer.writeFloat(1f);
                writer.writeFloat(1f);
                writer.writeFloat(0f);
                writer.writeFloat(0.7f);
            });
            audience.sendGroupedPacket(packet);

            distRemaining -= dirLength;
            current = current.add(dir);
        }
    }
}

package dev.emortal.minestom.lazertag.config;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

public class PositionJson {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public Vec asVec() {
        return new Vec(x, y, z);
    }
    public Pos asPos() {
        return new Pos(x, y, z, yaw, pitch);
    }
}
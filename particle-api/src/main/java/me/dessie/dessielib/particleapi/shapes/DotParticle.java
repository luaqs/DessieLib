package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;

public class DotParticle extends Shape {
    public DotParticle(ParticleData particle, int points) {
        super(particle, points, ((location, step) -> new Point3D(location.getX(), location.getY(), location.getZ())));
    }
}

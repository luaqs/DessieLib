package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;

public class CircleParticle extends Shape {
    public CircleParticle(ParticleData particle, int points, int radius) {
        this(particle, points, radius, true, false, true);
    }

    public CircleParticle(ParticleData particle, int points, int radius, boolean onX, boolean onY, boolean onZ) {
        super(particle, points, (((location, step) -> {
            double cos = radius * Math.cos(Math.PI * 2 * (step * ((double) 360 / points)) / 360);
            double sin = radius * Math.sin(Math.PI * 2 * (step * ((double) 360 / points)) / 360);
            return new Point3D(
                    location.getX() + (onX ? cos : 0),
                    location.getY() + (onY ? (onX ? sin : onZ ? cos : 0) : 0),
                    location.getZ() + (onZ ? sin : 0));
        })));
    }

}

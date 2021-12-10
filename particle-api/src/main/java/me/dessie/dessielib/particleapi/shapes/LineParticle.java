package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LineParticle extends Shape {
    public LineParticle(ParticleData particle, int points, Location toLocation) {
       this(particle, points, toLocation.toVector());
    }

    public LineParticle(ParticleData particle, int points, Vector toVector) {
        super(particle, points, (((location, step) -> {
            double slopeX = toVector.getX() - location.getX();
            double slopeY = toVector.getY() - location.getY();
            double slopeZ = toVector.getZ() - location.getZ();

            return new Point3D(location.getX() + (slopeX / points * step),
                    location.getY() + (slopeY / points * step),
                    location.getZ() + (slopeZ / points * step));
        })));
    }
}

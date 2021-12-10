package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.ShapedParticle;
import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.function.BiFunction;

public abstract class Shape extends ShapedParticle {
    public Shape(Particle particle, int points, BiFunction<Location, Integer, Point3D> shapeFunction) {
        super(particle, points, shapeFunction);
    }

    public Shape(ParticleData data, int points, BiFunction<Location, Integer, Point3D> shapeFunction) {
        super(data, points, shapeFunction);
    }
}

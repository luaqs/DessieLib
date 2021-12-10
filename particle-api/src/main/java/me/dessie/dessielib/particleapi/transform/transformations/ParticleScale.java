package me.dessie.dessielib.particleapi.transform.transformations;

import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.transform.TransformType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.BiFunction;

public class ParticleScale extends ParticleTransform {
    public ParticleScale(TransformType type, int frames, BiFunction<Location, Integer, Point3D> transform) {
        super(type, frames, transform);
    }

    @Override
    public void applyToPoints(Location location, List<Point3D> points) {
        Point3D scaleFactors = this.apply(location);

        for(Point3D point : points) {
            Vector vector = new Vector(point.getX(), point.getY(), point.getZ()).subtract(location.toVector());
            vector.multiply(new Vector(scaleFactors.getX(), scaleFactors.getY(), scaleFactors.getZ()));
            point.add(new Point3D(vector.getX(), vector.getY(), vector.getZ()));
        }
    }
}

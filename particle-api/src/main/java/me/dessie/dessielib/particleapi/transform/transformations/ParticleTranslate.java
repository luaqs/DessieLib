package me.dessie.dessielib.particleapi.transform.transformations;

import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.transform.TransformType;
import org.bukkit.Location;

import java.util.List;
import java.util.function.BiFunction;

public class ParticleTranslate extends ParticleTransform {
    public ParticleTranslate(TransformType type, int frames, BiFunction<Location, Integer, Point3D> transform) {
        super(type, frames, transform);
    }

    @Override
    public void applyToPoints(Location location, List<Point3D> points) {
        Point3D transform = this.apply(location);
        for(Point3D point : points) {
            point.add(transform);
        }
    }
}

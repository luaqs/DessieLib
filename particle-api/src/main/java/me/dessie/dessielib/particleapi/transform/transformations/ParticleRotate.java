package me.dessie.dessielib.particleapi.transform.transformations;

import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.transform.TransformType;
import org.bukkit.Location;

import java.util.List;
import java.util.function.BiFunction;

public class ParticleRotate extends ParticleTransform {

    private Point3D offset;

    public ParticleRotate(TransformType type, int frames, BiFunction<Location, Integer, Point3D> transform) {
        this(type, frames, transform, new Point3D(0, 0, 0));
    }

    public ParticleRotate(TransformType type, int frames, BiFunction<Location, Integer, Point3D> transform, Point3D offset) {
        super(type, frames, transform);
        this.offset = offset;
    }

    @Override
    public void applyToPoints(Location location, List<Point3D> points) {
        Point3D rotateDegrees = this.apply(location);

        for (Point3D point : points) {
            Point3D origin = new Point3D(location.getX(), location.getY(), location.getZ()).add(this.offset);

            /*
            I'm like 99% sure doing it this way makes it so if you rotate on more than
            one axis it rotates incorrectly.

            This should still be fine for rotating on a singular axis, and perhaps chaining multiple
            ParticleRotate will allow for a more accurate 3D rotation.

            However I have no idea what I'm doing so this is gonna have to do for now!

            Edit: Rotating on more than 1 axis seems to have a reasonable effect
            */
            Point3D rotated = rotateAroundX(origin, point, rotateDegrees.getX());
            rotated = rotateAroundY(origin, rotated, rotateDegrees.getY());
            rotated = rotateAroundZ(origin, rotated, rotateDegrees.getZ());

            point.setX(rotated.getX()).setY(rotated.getY()).setZ(rotated.getZ());
        }
    }

    private Point3D rotateAroundX(Point3D origin, Point3D point, double angle) {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));
        double y = (point.getY() - origin.getY()) * angleCos - (point.getZ() - origin.getZ()) * angleSin + origin.getY();
        double z = (point.getY() - origin.getY()) * angleSin + (point.getZ() - origin.getZ()) * angleCos + origin.getZ();
        return new Point3D(point.getX(), y, z);
    }

    private Point3D rotateAroundY(Point3D origin, Point3D point, double angle) {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));
        double x = angleSin * (point.getZ() - origin.getZ()) + angleCos * (point.getX() - origin.getX()) + origin.getX();
        double z = angleCos * (point.getZ() - origin.getZ()) - angleSin * (point.getX() - origin.getX()) + origin.getZ();
        return new Point3D(x, point.getY(), z);
    }

    private Point3D rotateAroundZ(Point3D origin, Point3D point, double angle) {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));
        double x = angleCos * (point.getX() - origin.getX()) - angleSin * (point.getY() - origin.getY()) + origin.getX();
        double y = angleSin * (point.getX() - origin.getX()) + angleCos * (point.getY() - origin.getY()) + origin.getY();
        return new Point3D(x, y, point.getZ());
    }
}

package me.dessie.dessielib.particleapi.point;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Point3D {

    double x;
    double y;
    double z;
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

    public Point3D setX(double x) {
        this.x = x;
        return this;
    }

    public Point3D setY(double y) {
        this.y = y;
        return this;
    }

    public Point3D setZ(double z) {
        this.z = z;
        return this;
    }

    public Point3D add(Point3D point) {
        return this.add(point.getX(), point.getY(), point.getZ());
    }

    public Point3D add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Point3D multiply(Point3D point) {
        return this.multiply(point.getX(), point.getY(), point.getZ());
    }

    public Point3D multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector toVector() {
        return new Vector(this.getX(), this.getY(), this.getZ());
    }

    public static List<Point3D> copyOf(List<Point3D> points) {
        List<Point3D> newPoints = new ArrayList<>();
        for(Point3D point : points) {
            newPoints.add(new Point3D(point.getX(), point.getY(), point.getZ()));
        }
        return newPoints;
    }

    @Override
    public String toString() {
        return "Point3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}

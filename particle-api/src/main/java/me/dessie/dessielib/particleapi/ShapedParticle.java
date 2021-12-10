package me.dessie.dessielib.particleapi;

import me.dessie.dessielib.particleapi.animation.ParticleAnimator;
import me.dessie.dessielib.particleapi.collison.ParticleCollider;
import me.dessie.dessielib.particleapi.point.Point3D;
import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ShapedParticle {
    private Particle particle;
    private Object particleOptions;

    private int points;
    private BiFunction<Location, Integer, Point3D> shapeFunction;
    private double particleSpeed;
    private final List<ParticleTransform> transforms = new ArrayList<>();
    private List<ParticleCollider> colliders = new ArrayList<>();
    private ParticleAnimator animator;

    public ShapedParticle(Particle particle, int points, BiFunction<Location, Integer, Point3D> shapeFunction) {
        this(new ParticleData(particle, null), points, shapeFunction);
    }

    public ShapedParticle(ParticleData data, int points, BiFunction<Location, Integer, Point3D> shapeFunction) {
        this.particle = data.getParticle();
        this.particleOptions = data.getOptions();
        this.points = points;
        this.shapeFunction = shapeFunction;
        this.particleSpeed = 0;

        this.setAnimator(new ParticleAnimator(5, 1));
    }

    public Particle getParticle() { return particle; }
    public BiFunction<Location, Integer, Point3D> getShapeFunction() { return shapeFunction; }
    public int getPoints() { return points; }
    public double getParticleSpeed() { return particleSpeed; }
    public List<ParticleTransform> getTransforms() { return transforms; }
    public ParticleAnimator getAnimator() { return animator; }
    public Object getParticleOptions() { return particleOptions; }
    public List<ParticleCollider> getColliders() { return colliders; }

    public ShapedParticle setParticleSpeed(double particleSpeed) {
        this.particleSpeed = particleSpeed;
        return this;
    }

    public ShapedParticle addTransform(ParticleTransform transform) {
        this.getTransforms().add(transform);
        return this;
    }

    public ShapedParticle setAnimator(ParticleAnimator animator) {
        this.animator = animator;
        this.getAnimator().particle = this;
        return this;
    }

    public ShapedParticle setParticle(Particle particle) {
        this.particle = particle;
        return this;
    }

    public ShapedParticle setParticle(ParticleData data) {
        this.particle = data.getParticle();
        this.particleOptions = data.getOptions();
        return this;
    }

    public ShapedParticle setShapeFunction(BiFunction<Location, Integer, Point3D> shapeFunction) {
        this.shapeFunction = shapeFunction;
        return this;
    }

    public ShapedParticle addCollider(ParticleCollider collider) {
        this.getColliders().add(collider);
        return this;
    }

    public void display(Player player, Location location) {
        //Start the animation if it's not already running
        if(!this.getAnimator().isRunning()) {
            this.getAnimator().start(player, location);
        }
    }

    public void display(World world, Location location) {
        //Start the animation if it's not already running
        if(!this.getAnimator().isRunning()) {
            this.getAnimator().start(world, location);
        }
    }

    public List<Point3D> getPoints(Location location) {
        List<Point3D> points = new ArrayList<>();

        //Calculate each particle point, by applying them to the shape function.
        for (int i = 0; i < this.getPoints(); i++) {
            points.add(this.getShapeFunction().apply(location, i));
        }

        //Apply the Transformations
        this.getTransforms().forEach(transform -> {
            if(transform.isStatic()) {
                //Get a complete copy of the Shape Points.
                List<Point3D> temp = Point3D.copyOf(points);

                //Apply to the first set of points.
                transform.applyToPoints(location, points);

                //Now apply the transformation to each subsequent frame.
                List<Point3D> toApply = Point3D.copyOf(temp);
                for(int i = 1; i < transform.getFrames(); i++) {
                    transform.applyToPoints(location, toApply);
                    points.addAll(toApply);
                    toApply = Point3D.copyOf(temp);
                }
            } else {
                transform.applyToPoints(location, points);
            }
        });

        this.getColliders().forEach(collider -> {
            collider.startCollide(this, location.getWorld(), points);
        });

        return points;
    }
}

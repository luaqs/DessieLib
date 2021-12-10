package me.dessie.dessielib.particleapi.collison;

import me.dessie.dessielib.particleapi.ShapedParticle;
import me.dessie.dessielib.particleapi.point.Point3D;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ParticleCollider {
    private int delay;

    //Determines if this Collider can activate an infinite amount of times per tick.
    //If it's false, an Object will only be collided with a maximum one time per frame.
    //If this is true, and an object collides with multiple particles,
    //this will fire for all colliding particles.
    private boolean multiCollide;

    //Keeps track of the current frame collisions.
    //Is not used if multiCollide is true.
    private List<Object> frameCollisions;

    private final Map<Object, Integer> delays = new HashMap<>();

    public ParticleCollider(int delay) {
        this(delay, false);
    }

    public ParticleCollider(int delay, boolean multiCollide) {
        this.delay = delay;
        this.multiCollide = multiCollide;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isMultiCollide() {
        return multiCollide;
    }

    public ParticleCollider setMultiCollide(boolean multiCollide) {
        this.multiCollide = multiCollide;
        return this;
    }

    public ParticleCollider setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    protected void add(Object object) {
        delays.put(object, this.getDelay());

        //Add this Object to the frame collision.
        if(!this.isMultiCollide()) {
            this.frameCollisions.add(object);
        }
    }

    protected boolean canCollide(Object object) {
        return !delays.containsKey(object) && (!this.isMultiCollide() && !frameCollisions.contains(object));
    }

    protected void doDelayCalculate(ShapedParticle particle) {
        //Decrement the Collision Delay for all entities by the Particle's Animation Speed.
        for(Object object : delays.keySet()) {
            delays.compute(object, ((object1, delay) -> delay -= particle.getAnimator().getAnimationSpeed()));
        }

        //Remove them from this delay if their delay is lower than 0.
        delays.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    public void startCollide(ShapedParticle particle, World world, List<Point3D> points) {
        this.doDelayCalculate(particle);
        this.frameCollisions = new ArrayList<>();

        this.attemptCollide(particle, world, points);
    }

    protected abstract void attemptCollide(ShapedParticle particle, World world, List<Point3D> points);
}

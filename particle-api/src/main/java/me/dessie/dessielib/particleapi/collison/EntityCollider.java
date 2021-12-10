package me.dessie.dessielib.particleapi.collison;

import me.dessie.dessielib.particleapi.ShapedParticle;
import me.dessie.dessielib.particleapi.point.Point3D;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class EntityCollider extends ParticleCollider {

    private Consumer<Entity> collider;

    public EntityCollider(Consumer<Entity> collider, int delay) {
        this(collider, delay, false);
    }

    public EntityCollider(Consumer<Entity> collider, int delay, boolean isMultiCollide) {
        super(delay, isMultiCollide);
        this.collider = collider;
    }

    public Consumer<Entity> getCollider() {
        return collider;
    }

    public ParticleCollider setCollider(Consumer<Entity> collider) {
        this.collider = collider;
        return this;
    }

    @Override
    public void attemptCollide(ShapedParticle particle, World world, List<Point3D> points) {
        //For all points, attempt to find any Entity that collides with this point.
        for(Point3D point : points) {
            //So first we can get the Chunk that this point is in
            Chunk chunk = world.getChunkAt((int) point.getX() >> 4, (int) point.getZ() >> 4);
            if(!chunk.isLoaded()) continue;

            //Look through all the Chunk's entities & attempt a collision
            //Provided that the entity is valid for a collision attempt.
            Arrays.stream(chunk.getEntities())
                    .filter(entity -> this.canCollide(entity) && entity.getBoundingBox().contains(point.toVector()))
                    .forEach(entity -> {
                        this.getCollider().accept(entity);
                        this.add(entity);
                    });
        }
    }
}

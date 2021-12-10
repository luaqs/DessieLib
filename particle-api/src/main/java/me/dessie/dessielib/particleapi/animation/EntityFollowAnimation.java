package me.dessie.dessielib.particleapi.animation;

import me.dessie.dessielib.particleapi.point.Point3D;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * An Animator that will track the Location of a specific Entity.
 * This means that the Location object passed into the created ShapedParticle
 * Will be modified to "follow" the passed Entity's location.
 */
public class EntityFollowAnimation extends ParticleAnimator {

    private Entity entity;
    private Point3D offset;

    public EntityFollowAnimation(Entity entity, int animationSpeed) {
        this(entity, animationSpeed, 0);
    }

    public EntityFollowAnimation(Entity entity, int animationSpeed, int loops) {
        this(entity, animationSpeed, loops, new Point3D(0, 0, 0));
    }

    public EntityFollowAnimation(Entity entity, int animationSpeed, int loops, Point3D offset) {
        super(animationSpeed, loops);
        this.entity = entity;
        this.offset = offset;
    }

    public Entity getEntity() { return entity; }

    public EntityFollowAnimation setEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public Point3D getOffset() {
        return offset;
    }

    public EntityFollowAnimation setOffset(Point3D offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public void run() {
        Location location = this.getEntity().getLocation();
        this.location.setX(location.getX() + this.getOffset().getX());
        this.location.setY(location.getY() + this.getOffset().getY());
        this.location.setZ(location.getZ() + this.getOffset().getZ());
        super.run();
    }
}

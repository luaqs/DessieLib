package me.dessie.dessielib.particleapi.collison;

import me.dessie.dessielib.particleapi.ShapedParticle;
import me.dessie.dessielib.particleapi.point.Point3D;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.function.Consumer;

public class BlockCollider extends ParticleCollider {

    private Consumer<Block> collider;

    public BlockCollider(Consumer<Block> collider, int delay) {
        this(collider, delay, false);
    }

    public BlockCollider(Consumer<Block> collider, int delay, boolean multiCollide) {
        super(delay, multiCollide);
        this.collider = collider;
    }

    public Consumer<Block> getCollider() {
        return collider;
    }

    public ParticleCollider setCollider(Consumer<Block> collider) {
        this.collider = collider;
        return this;
    }

    @Override
    protected void attemptCollide(ShapedParticle particle, World world, List<Point3D> points) {
        for(Point3D point : points) {
            Block block = world.getBlockAt((int) point.getX(), (int) point.getY(), (int) point.getZ());
            if(block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) continue;

            this.getCollider().accept(block);
            this.add(block);
        }
    }
}

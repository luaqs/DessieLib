package me.dessie.dessielib.particleapi.animation;

import me.dessie.dessielib.particleapi.ParticleAPI;
import me.dessie.dessielib.particleapi.ShapedParticle;
import me.dessie.dessielib.particleapi.point.Point3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ParticleAnimator extends BukkitRunnable {

    public ShapedParticle particle;

    //Data for re-applying the Particle's draw methods.
    protected Player player;
    protected World world;
    protected Location location;

    //How many loops the Animator does before automatically cancelling.
    //Setting to 0 will loop forever.
    private int loops;

    //Tracks how far along the Animator is
    private int currentLoop;

    private boolean running = false;
    private int animationSpeed;

    public ParticleAnimator(int animationSpeed) {
        this(animationSpeed, 0);
    }

    public ParticleAnimator(int animationSpeed, int loops) {
        this.animationSpeed = animationSpeed;
        this.loops = loops;
    }

    public int getAnimationSpeed() { return animationSpeed; }
    public int getLoops() { return loops; }

    public ShapedParticle getParticle() { return particle; }
    public boolean isRunning() { return running; }

    public ParticleAnimator setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
        return this;
    }

    public ParticleAnimator setLoops(int loops) {
        this.loops = loops;
        return this;
    }

    public void start(Player player, Location location) {
        this.player = player;
        start(location);
    }

    public void start(World world, Location location) {
        this.world = world;
        start(location);
    }

    public void stop() {
        this.cancel();
        this.world = null;
        this.player = null;
        this.location = null;
        this.running = false;
    }

    @Override
    public void run() {
        if(this.getLoops() != 0 && this.currentLoop >= this.getLoops()) {
            this.cancel();
            return;
        }

        //Display the next Iteration.
        display(this.getParticle().getPoints(this.location));
        this.currentLoop++;
    }

    private void start(Location location) {
        if(ParticleAPI.getPlugin() == null) throw new IllegalStateException("ParticleAPI not registered!");
        if(this.isRunning()) throw new IllegalStateException("Already running!");

        this.location = location;
        this.running = true;
        this.runTaskTimer(ParticleAPI.getPlugin(), 0, this.getAnimationSpeed());
    }

    private void display(List<Point3D> points) {
        if(this.location != null && this.player != null) {
            for(Point3D point : points) {
                this.player.spawnParticle(this.getParticle().getParticle(), point.getX(), point.getY(), point.getZ(), 1, 0, 0 ,0, this.getParticle().getParticleSpeed(), this.getParticle().getParticleOptions());
            }
        } else if(this.location != null && this.world != null) {
            for(Point3D point : points) {
                this.world.spawnParticle(this.getParticle().getParticle(), point.getX(), point.getY(), point.getZ(), 1, 0, 0 ,0, this.getParticle().getParticleSpeed(), this.getParticle().getParticleOptions());
            }
        }
    }
}

package me.dessie.dessielib.particleapi.transform;

import me.dessie.dessielib.particleapi.point.Point3D;
import org.bukkit.Location;

import java.util.List;
import java.util.function.BiFunction;

public abstract class ParticleTransform {
    private int frames;
    private int currentStep = 0;
    private BiFunction<Location, Integer, Point3D> transform;
    private TransformType type;
    private boolean isOscillating = false;

    public ParticleTransform(TransformType type, int frames, BiFunction<Location, Integer, Point3D> transform) {
        this.type = type;
        this.frames = frames;
        this.transform = transform;
    }

    protected Point3D apply(Location location) {
        Point3D point = this.getTransform().apply(location, this.currentStep);
        
        if(this.getType() == TransformType.OSCILLATE) {
            if (this.isOscillating) {
                this.currentStep--;
                if(this.currentStep <= 0) {
                    this.isOscillating = false;
                }
            } else {
                this.currentStep++;
                if(this.currentStep > this.getFrames()) {
                    this.isOscillating = true;
                }
            }
        } else if(this.getType() == TransformType.RESTART || this.getType() == TransformType.STATIC) {
            this.currentStep++;
            if(this.currentStep > this.getFrames()) {
                this.currentStep = 0;
            }
        }

        return point;
    }

    public BiFunction<Location, Integer, Point3D> getTransform() {
        return transform;
    }

    public TransformType getType() {
        return type;
    }
    public int getFrames() {
        return frames;
    }
    public boolean isStatic() { return this.getType() == TransformType.STATIC; }

    public ParticleTransform setType(TransformType type) {
        this.type = type;
        return this;
    }

    public void setTransform(BiFunction<Location, Integer, Point3D> transform) {
        this.transform = transform;
    }

    public abstract void applyToPoints(Location location, List<Point3D> points);
}

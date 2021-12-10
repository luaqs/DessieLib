package me.dessie.dessielib.particleapi.wrapper;

import org.bukkit.Particle;

public class ParticleData {

    private Particle particle;
    private Object options;

    public ParticleData(Particle particle) {
        this(particle, null);
    }

    public ParticleData(Particle particle, Object options) {
        this.particle = particle;
        this.options = options;
    }

    public Particle getParticle() {
        return particle;
    }
    public Object getOptions() {
        return options;
    }
}

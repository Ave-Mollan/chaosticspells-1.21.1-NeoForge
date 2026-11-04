package com.avemollan.chaosticspells.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Glowing crimson ember in the Chaos school colours. Spawned by {@link MagicDarkMetalAuraParticles}; it circles the
 * point it was spawned around ({@code xd} = orbit centre x, {@code zd} = orbit centre z, {@code yd} = rise speed),
 * drifts up, animates through its sprite frames and fades out.
 */
public class ChaosEmberParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double centerX;
    private final double centerZ;
    private final double radius;
    private final float angularSpeed;
    private final float baseSize;
    private float angle;

    protected ChaosEmberParticle(ClientLevel level, double x, double y, double z, double centerX, double rise, double centerZ,
            SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.centerX = centerX;
        this.centerZ = centerZ;
        double dx = x - centerX;
        double dz = z - centerZ;
        this.radius = Math.sqrt(dx * dx + dz * dz);
        this.angle = (float) Mth.atan2(dz, dx);
        this.angularSpeed = (this.random.nextBoolean() ? 1 : -1) * (0.06f + this.random.nextFloat() * 0.06f);
        this.xd = 0.0;
        this.yd = rise;
        this.zd = 0.0;
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.friction = 1.0f;
        this.lifetime = 24 + this.random.nextInt(20);
        this.baseSize = 0.05f + this.random.nextFloat() * 0.035f;
        this.quadSize = this.baseSize;
        float shade = 0.85f + this.random.nextFloat() * 0.15f;
        this.setColor(shade, shade, shade);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.angle += this.angularSpeed;
        double r = this.radius * (1.0 - 0.25 * this.age / (double) this.lifetime);
        this.setPos(this.centerX + Math.cos(this.angle) * r, this.y + this.yd, this.centerZ + Math.sin(this.angle) * r);
        float life = this.age / (float) this.lifetime;
        this.alpha = life < 0.7f ? 1.0f : 1.0f - (life - 0.7f) / 0.3f;
        this.quadSize = this.baseSize * (1.0f - 0.4f * life);
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0; // full bright: the embers glow in the dark
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new ChaosEmberParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}

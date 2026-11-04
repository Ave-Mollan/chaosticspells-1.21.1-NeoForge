package com.avemollan.chaosticspells.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Magic glyph in the Chaos school colours (enchanting-table style symbols, own pixel art). Spawned by the server
 * ({@code MagicDarkMetalAura}); velocity slots carry the orbit centre x, rise speed and orbit centre z. Each particle
 * picks a random glyph, circles the centre, rises, fades in and out and glows in the dark.
 */
public class ChaosGlyphParticle extends TextureSheetParticle {
    /** Chaos palette tints applied to the white glyph sprites: crimson, deep red, ember highlight. */
    private static final float[][] TINTS = {
            {215 / 255f, 51 / 255f, 53 / 255f},
            {179 / 255f, 27 / 255f, 37 / 255f},
            {1.0f, 148 / 255f, 128 / 255f},
            {149 / 255f, 20 / 255f, 35 / 255f}
    };

    private final double centerX;
    private final double centerZ;
    private final double radius;
    private final float angularSpeed;
    private float angle;

    protected ChaosGlyphParticle(ClientLevel level, double x, double y, double z, double centerX, double rise, double centerZ,
            SpriteSet sprites) {
        super(level, x, y, z);
        this.centerX = centerX;
        this.centerZ = centerZ;
        double dx = x - centerX;
        double dz = z - centerZ;
        this.radius = Math.sqrt(dx * dx + dz * dz);
        this.angle = (float) Mth.atan2(dz, dx);
        this.angularSpeed = (this.random.nextBoolean() ? 1 : -1) * (0.04f + this.random.nextFloat() * 0.04f);
        this.xd = 0.0;
        this.yd = rise;
        this.zd = 0.0;
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.friction = 1.0f;
        this.lifetime = 30 + this.random.nextInt(20);
        this.quadSize = 0.09f + this.random.nextFloat() * 0.04f;
        float[] tint = TINTS[this.random.nextInt(TINTS.length)];
        this.setColor(tint[0], tint[1], tint[2]);
        this.alpha = 0.0f;
        this.pickSprite(sprites);
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
        this.setPos(this.centerX + Math.cos(this.angle) * this.radius, this.y + this.yd, this.centerZ + Math.sin(this.angle) * this.radius);
        float life = this.age / (float) this.lifetime;
        this.alpha = life < 0.15f ? life / 0.15f : life > 0.7f ? 1.0f - (life - 0.7f) / 0.3f : 1.0f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double centerX, double rise, double centerZ) {
            Minecraft mc = Minecraft.getInstance();
            Entity camera = mc.getCameraEntity();
            // Own aura in first person: skip glyphs at face height so they do not float in front of the camera.
            if (camera != null && mc.options.getCameraType() == CameraType.FIRST_PERSON
                    && Mth.square(centerX - camera.getX()) + Mth.square(centerZ - camera.getZ()) < 1.0
                    && y > camera.getY() + 1.1) {
                return null;
            }
            return new ChaosGlyphParticle(level, x, y, z, centerX, rise, centerZ, this.sprites);
        }
    }
}

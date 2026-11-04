package com.avemollan.chaosticspells.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.avemollan.chaosticspells.ChaosticSpells;

public final class ChaosticParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ChaosticSpells.MODID);

    /** Chaos-coloured magic glyph circling a player while the Magic Dark Metal low-health effect is active. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CHAOS_GLYPH =
            PARTICLE_TYPES.register("chaos_glyph", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

    private ChaosticParticleRegistry() {
    }
}

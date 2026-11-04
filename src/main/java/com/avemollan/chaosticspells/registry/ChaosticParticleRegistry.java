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

    /** Crimson chaos ember that circles a player wearing the full Magic Dark Metal set (client-side only). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CHAOS_EMBER =
            PARTICLE_TYPES.register("chaos_ember", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

    private ChaosticParticleRegistry() {
    }
}

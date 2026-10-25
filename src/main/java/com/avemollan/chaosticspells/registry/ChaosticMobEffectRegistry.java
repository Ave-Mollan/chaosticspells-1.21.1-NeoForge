package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.effect.DeathPactEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChaosticMobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ChaosticSpells.MODID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    public static final DeferredHolder<MobEffect, MobEffect> DEATH_PACT =
            MOB_EFFECTS.register("death_pact", DeathPactEffect::new);

    private ChaosticMobEffectRegistry() {
    }
}

package com.avemollan.chaosticspells.events;

import com.avemollan.chaosticspells.registry.ChaosticMobEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class DeathPactEventHandler {

    private static final String BORN_IN_CHAOS_ID = "born_in_chaos_v1";
    private static final ResourceKey<MobEffect> MAGIC_DEPLETION =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "magic_depletion"));

    private DeathPactEventHandler() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (!entity.hasEffect(ChaosticMobEffectRegistry.DEATH_PACT)) {
            return;
        }
        if (hasMagicDepletion(entity)) {
            return;
        }

        event.setCanceled(true);

        entity.removeEffect(ChaosticMobEffectRegistry.DEATH_PACT);
        entity.setHealth(3.0f);
        entity.clearFire();
        if (entity instanceof Player player) {
            player.getFoodData().setFoodLevel(1);
        }

        var level = entity.level();
        level.playSound(null, entity.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.playSound(null, entity.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.3f, 1.2f);

        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 4, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2, false, false, true));

        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getHolder(MAGIC_DEPLETION).ifPresent(holder ->
                    entity.addEffect(new MobEffectInstance(holder, 200, 0, false, false, true)));
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    15,
                    0.8,
                    0.8,
                    0.8,
                    0.1);
        }
    }

    private static boolean hasMagicDepletion(LivingEntity entity) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return false;
        }
        return entity.level().registryAccess().registryOrThrow(Registries.MOB_EFFECT).getHolder(MAGIC_DEPLETION).map(entity::hasEffect).orElse(false);
    }
}

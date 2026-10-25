package com.avemollan.chaosticspells.util;

import com.avemollan.chaosticspells.entity.ChaoticArrowProjectile;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class ChaoticArrowEffects {
    private static final String BORN_IN_CHAOS_ID = "born_in_chaos_v1";
    private static final List<ResourceKey<MobEffect>> BORN_IN_CHAOS_EFFECTS = List.of(
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "bone_fracture")),
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "cursed_mark")),
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "stun")),
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "soul_stratification")),
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "bone_chilling")),
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "intoxication")),
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "infernal_flame"))
    );

    private ChaoticArrowEffects() {
    }

    public static void applyRandomChaoticEffect(LivingEntity target, int spellLevel) {
        List<Holder<MobEffect>> pool = buildPool(target.level());
        if (pool.isEmpty()) {
            return;
        }
        Random random = ThreadLocalRandom.current();
        Holder<MobEffect> effect = pool.get(random.nextInt(pool.size()));
        int duration = (5 + spellLevel * 3) * 20;
        int amplifier = Math.max(0, (int) Math.floor(spellLevel / 2.0f));
        if (effect.value().isInstantenous()) {
            target.addEffect(new MobEffectInstance(effect, 1, 0));
        } else {
            target.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    public static void spawnFlightParticle(ChaoticArrowProjectile projectile) {
        var random = projectile.level().random;
        int[] potionColors = {
                0x7F00FF, 0x00FF00, 0xFF0000, 0xFFFF00, 0x00FFFF, 0xFF00FF, 0x0000FF, 0x8B0000
        };
        int color = potionColors[random.nextInt(potionColors.length)];
        double d0 = (double) (color >> 16 & 255) / 255.0D;
        double d1 = (double) (color >> 8 & 255) / 255.0D;
        double d2 = (double) (color & 255) / 255.0D;
        var particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) d0, (float) d1, (float) d2);
        projectile.level().addParticle(
                particle,
                projectile.getX() + random.nextDouble() * 0.2 - 0.1,
                projectile.getY() + random.nextDouble() * 0.2 - 0.1,
                projectile.getZ() + random.nextDouble() * 0.2 - 0.1,
                0.0D,
                0.0D,
                0.0D);
    }

    private static List<Holder<MobEffect>> buildPool(Level level) {
        var list = new ArrayList<Holder<MobEffect>>();
        list.add(MobEffects.MOVEMENT_SLOWDOWN);
        list.add(MobEffects.HARM);
        list.add(MobEffects.CONFUSION);
        list.add(MobEffects.BLINDNESS);
        list.add(MobEffects.HUNGER);
        list.add(MobEffects.WEAKNESS);
        list.add(MobEffects.POISON);
        list.add(MobEffects.WITHER);
        list.add(MobEffects.DARKNESS);

        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            var registry = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
            for (ResourceKey<MobEffect> key : BORN_IN_CHAOS_EFFECTS) {
                registry.getHolder(key).ifPresent(list::add);
            }
        }
        return list;
    }
}

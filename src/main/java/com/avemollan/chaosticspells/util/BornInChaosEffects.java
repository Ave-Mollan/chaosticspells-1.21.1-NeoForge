package com.avemollan.chaosticspells.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import java.util.Optional;

public final class BornInChaosEffects {
    private static final String BORN_IN_CHAOS_ID = "born_in_chaos_v1";

    private BornInChaosEffects() {
    }

    private static final ResourceKey<MobEffect> OBSESSION =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "obsession"));

    private static final ResourceKey<MobEffect> MAGIC_DEPLETION =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "magic_depletion"));

    private static final ResourceKey<MobEffect> FURIOUS_RAMPAGE =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "furious_rampage"));

    private static final ResourceLocation LORD_PUMPKIN_HAT_ID =
            ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "lord_pumpkinheads_hat_helmet");

    public static boolean isLordPumpkinHatEquipped(LivingEntity entity) {
        if (entity == null || !ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return false;
        }
        return BuiltInRegistries.ITEM.getOptional(LORD_PUMPKIN_HAT_ID)
                .map(item -> entity.getItemBySlot(EquipmentSlot.HEAD).is(item))
                .orElse(false);
    }

    public static void applyPumpkinBarrageExplosionAoe(ServerLevel level, Entity explosionSource, LivingEntity owner, Vec3 center) {
        boolean lordHat = owner != null && isLordPumpkinHatEquipped(owner);
        double radius = lordHat ? 3.0D : 2.0D;
        float playerDamage = 1.0f;
        float mobDamage = lordHat ? 6.0f : 3.0f;

        Optional<Item> lordItem = ModList.get().isLoaded(BORN_IN_CHAOS_ID)
                ? BuiltInRegistries.ITEM.getOptional(LORD_PUMPKIN_HAT_ID)
                : Optional.empty();

        AABB box = new AABB(center, center).inflate(radius);
        DamageSource damageSource = level.damageSources().explosion(explosionSource, owner);
        for (Entity entity : level.getEntities(explosionSource, box, e -> true)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (lordItem.isPresent() && living.getItemBySlot(EquipmentSlot.HEAD).is(lordItem.get())) {
                continue;
            }
            boolean isMob = entity instanceof Mob;
            boolean isMonster = entity instanceof Monster;
            boolean isAnimal = entity instanceof Animal;
            boolean isPlayer = entity instanceof Player;
            if (!isMob && !isMonster && !isAnimal && !isPlayer) {
                continue;
            }
            float amount = isPlayer ? playerDamage : mobDamage;
            living.hurt(damageSource, amount);
        }
    }

    /** Same payload as Born in Chaos MissionaryHatpProcedure ({@code MAGIC_DEPLETION}, 300 ticks, amplifier 0). */
    public static void applyMissionaryHatMagicDepletion(LivingEntity target, Level level) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID) || level.isClientSide()) {
            return;
        }
        level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getHolder(MAGIC_DEPLETION).ifPresent(
                holder -> target.addEffect(new MobEffectInstance(holder, 300, 0)));
    }

    public static boolean hasMagicDepletion(LivingEntity entity) {
        if (entity == null || !ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return false;
        }
        return entity.level()
                .registryAccess()
                .registryOrThrow(Registries.MOB_EFFECT)
                .getHolder(MAGIC_DEPLETION)
                .map(entity::hasEffect)
                .orElse(false);
    }

    /**
     * Multiplier applied by ChaosticSpells to Iron's Spellbooks passive mana regeneration inside
     * {@code MagicManager.regenPlayerMana}. Contributions from magic depletion, obsession, and furious rampage
     * multiply together.
     *
     * <ul>
     *   <li>Magic depletion, display level L = amplifier + 1: {@code max(0, 1 − 50% × L)}</li>
     *   <li>Obsession: {@code 1 + (25% + 10% × amplifier)}</li>
     *   <li>Furious rampage: {@code 1 + (15% + 10% × amplifier)}</li>
     * </ul>
     */
    public static float getIronSpellbooksPassiveManaRegenMultiplier(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return 1.0f;
        }
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return 1.0f;
        }
        var registry = entity.level().registryAccess().registryOrThrow(Registries.MOB_EFFECT);

        float m = 1.0f;

        var depletionHolder = registry.getHolder(MAGIC_DEPLETION).orElse(null);
        if (depletionHolder != null) {
            MobEffectInstance inst = entity.getEffect(depletionHolder);
            if (inst != null) {
                float displayLevel = inst.getAmplifier() + 1.0f;
                m *= Math.max(0.0f, 1.0f - 0.5f * displayLevel);
            }
        }

        var obsessionHolder = registry.getHolder(OBSESSION).orElse(null);
        if (obsessionHolder != null) {
            MobEffectInstance inst = entity.getEffect(obsessionHolder);
            if (inst != null) {
                int amplifier = inst.getAmplifier();
                m *= 1.0f + (25 + 10 * amplifier) / 100.0f;
            }
        }

        var furiousHolder = registry.getHolder(FURIOUS_RAMPAGE).orElse(null);
        if (furiousHolder != null) {
            MobEffectInstance inst = entity.getEffect(furiousHolder);
            if (inst != null) {
                int amplifier = inst.getAmplifier();
                m *= 1.0f + (15 + 10 * amplifier) / 100.0f;
            }
        }

        return m;
    }

    public static void applyObsession(LivingEntity target, Level level) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        var registry = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
        registry.getHolder(OBSESSION).ifPresent(holder ->
                target.addEffect(new MobEffectInstance(holder, (int) (20 * 60 * 2.5), 0, false, true, true)));
    }

    public static void sendPumpkinStaffFlightTrail(ServerLevel level, double x, double y, double z) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        BuiltInRegistries.PARTICLE_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "pumpkin_trail")).ifPresent(pt -> {
            if (pt instanceof SimpleParticleType spt) {
                level.sendParticles(spt, x, y, z, 2, 0.1D, 0.1D, 0.1D, 0.1D);
            }
        });
    }

    public static void sendPumpkinStaffImpact(ServerLevel level, double x, double y, double z, boolean lordHatExplosion) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        int count = lordHatExplosion ? 5 : 3;
        double dx = lordHatExplosion ? 1.0D : 0.6D;
        double dy = lordHatExplosion ? 0.7D : 0.6D;
        double dz = lordHatExplosion ? 1.0D : 0.6D;
        BuiltInRegistries.PARTICLE_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "pumpkin_explosion")).ifPresent(pt -> {
            if (pt instanceof SimpleParticleType spt) {
                level.sendParticles(spt, x, y + 1.0D, z, count, dx, dy, dz, 0.1D);
            }
        });
        BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "pumpkin_hit")).ifPresent(sound ->
                level.playSound(null, BlockPos.containing(x, y + 1.0D, z), sound, SoundSource.NEUTRAL, 1.6f, 1.0f));
    }

    public static void playMagicStaffShoot(Level level, LivingEntity entity) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "magic_staff_shoot");
        BuiltInRegistries.SOUND_EVENT.getOptional(soundId).ifPresent(sound ->
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.NEUTRAL, 0.6f, 1.0f));
    }

    public static void sendRitualParticles(ServerLevel serverLevel, double x, double y, double z) {
        sendNamedParticle(serverLevel, "ritual", x, y + 1.0, z, 5, 0.3, 0.3, 0.3, 0.1);
    }

    public static void sendMagichit(ServerLevel serverLevel, Entity target) {
        sendNamedParticle(serverLevel, "magichit", target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.3, 0.4, 0.3, 0.1);
    }

    public static void addMagicTrailClient(Level level, double x, double y, double z) {
        if (!level.isClientSide || !ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        BuiltInRegistries.PARTICLE_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "magictrail")).ifPresent(pt -> {
            if (pt instanceof SimpleParticleType spt) {
                level.addParticle(spt, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        });
    }

    private static void sendNamedParticle(ServerLevel serverLevel, String name, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        BuiltInRegistries.PARTICLE_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, name)).ifPresent(pt -> {
            if (pt instanceof SimpleParticleType spt) {
                serverLevel.sendParticles(spt, x, y, z, count, dx, dy, dz, speed);
            }
        });
    }

    private static final ResourceKey<MobEffect> CURSED_MARK =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "cursed_mark"));

    public static void onFallenChaosKnightMeleeHit(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        var registry = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
        registry.getHolder(CURSED_MARK).ifPresent(holder -> {
            if (!target.hasEffect(holder)) {
                target.addEffect(new MobEffectInstance(holder, 3400, 0));
            }
        });
        BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "fallen_attack")).ifPresent(sound ->
                level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), sound, SoundSource.HOSTILE, 0.8f, 1.0f));
        BuiltInRegistries.PARTICLE_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "srirst_part")).ifPresent(pt -> {
            if (pt instanceof SimpleParticleType spt) {
                level.sendParticles(spt, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.5, 0.5, 0.5, 0.8);
            }
        });
    }
}

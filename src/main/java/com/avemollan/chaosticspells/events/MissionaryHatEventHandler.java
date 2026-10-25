package com.avemollan.chaosticspells.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import com.avemollan.chaosticspells.entity.RaisedBabySkeleton;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.avemollan.chaosticspells.util.BornInChaosEffects;

public final class MissionaryHatEventHandler {

    private static final String BORN_IN_CHAOS_ID = "born_in_chaos_v1";

    private MissionaryHatEventHandler() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || !(victim instanceof Player)) {
            return;
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity == null) {
            return;
        }

        if (!(sourceEntity instanceof Monster)) {
            return;
        }

        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID) && BornInChaosEffects.hasMagicDepletion(victim)) {
            return;
        }

        ItemStack helmet = victim.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.is(ChaosticItemRegistry.MISSIONARY_HAT_HELMET.get())) {
            return;
        }

        // BiC applies magic_depletion before the feet occlusion check; mirror that here.
        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            BornInChaosEffects.applyMissionaryHatMagicDepletion(victim, victim.level());
        }

        double x = victim.getX();
        double y = victim.getY();
        double z = victim.getZ();

        if (victim.level().getBlockState(BlockPos.containing(x, y, z)).canOcclude()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) victim.level();
        int summonTime = 20 * 60 * 10;

        RaisedBabySkeleton skeleton = new RaisedBabySkeleton(serverLevel, victim);
        skeleton.moveTo(x + 0.5, y, z, serverLevel.random.nextFloat() * 360.0f, 0.0f);
        EventHooks.finalizeMobSpawn(
                skeleton,
                serverLevel,
                serverLevel.getCurrentDifficultyAt(BlockPos.containing(x, y, z)),
                MobSpawnType.MOB_SUMMONED,
                null);

        serverLevel.addFreshEntity(skeleton);
        SummonManager.setDuration(skeleton, summonTime);

        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            BuiltInRegistries.PARTICLE_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "dimlarg")).ifPresent(
                    type -> {
                        if (type instanceof SimpleParticleType spt) {
                            serverLevel.sendParticles(spt, x + 0.5, y, z, 4, 0.1, 0.1, 0.1, 0.1);
                        }
                    });
            BuiltInRegistries.SOUND_EVENT
                    .getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "missionary_spawn_mobs"))
                    .ifPresent(
                            evt -> serverLevel.playSound(null, BlockPos.containing(x, y, z), evt, SoundSource.NEUTRAL, 0.4f, 1.0f));
        }
    }
}

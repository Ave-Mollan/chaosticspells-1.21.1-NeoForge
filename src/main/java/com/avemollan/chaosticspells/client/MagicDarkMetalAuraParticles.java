package com.avemollan.chaosticspells.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.item.MagicDarkMetalArmorItem;
import com.avemollan.chaosticspells.registry.ChaosticParticleRegistry;

/**
 * Chaos embers around every visible player who wears the complete Magic Dark Metal set (either helmet). Purely
 * visual and client-side: equipment is already synced, so other players see the aura too. Obeys the particle
 * setting through {@code ClientLevel.addParticle}.
 */
@EventBusSubscriber(modid = ChaosticSpells.MODID, value = Dist.CLIENT)
public final class MagicDarkMetalAuraParticles {
    private static final double MAX_DISTANCE_SQR = 48.0 * 48.0;
    /** Average embers per tick per player (about 9 per second). */
    private static final float SPAWN_CHANCE = 0.45f;

    private MagicDarkMetalAuraParticles() {}

    @SubscribeEvent
    static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ChaosticParticleRegistry.CHAOS_EMBER.get(), ChaosEmberParticle.Provider::new);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.isPaused() || mc.cameraEntity == null) {
            return;
        }
        RandomSource random = level.getRandom();
        for (Player player : level.players()) {
            if (player.isInvisible() || player.isSpectator()
                    || player.distanceToSqr(mc.cameraEntity) > MAX_DISTANCE_SQR
                    || !MagicDarkMetalArmorItem.hasFullSet(player)
                    || random.nextFloat() >= SPAWN_CHANCE) {
                continue;
            }
            boolean ownFirstPerson = player == mc.cameraEntity && mc.options.getCameraType() == CameraType.FIRST_PERSON;
            double maxHeight = ownFirstPerson ? 1.1 : player.getBbHeight() * 1.05;
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 0.55 + random.nextDouble() * 0.35;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + 0.1 + random.nextDouble() * maxHeight;
            double rise = 0.008 + random.nextDouble() * 0.018;
            // Velocity slots carry the orbit centre and rise speed, see ChaosEmberParticle.
            level.addParticle(ChaosticParticleRegistry.CHAOS_EMBER.get(), x, y, z, player.getX(), rise, player.getZ());
        }
    }
}

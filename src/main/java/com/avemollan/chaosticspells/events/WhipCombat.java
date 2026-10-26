package com.avemollan.chaosticspells.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.network.WhipActionPayload;
import com.avemollan.chaosticspells.network.WhipMotionPayload;
import com.avemollan.chaosticspells.physics.WhipArmPose;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.avemollan.chaosticspells.registry.WhipSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WhipCombat {
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "owners_whip"));
    private record Charge(ItemStack stack, int slot, ResourceKey<Level> dimension, long started) {}
    private static final Map<UUID, Charge> CHARGES = new HashMap<>();
    private static final Map<UUID, Boolean> NEXT_LEFT = new HashMap<>();
    private static final class Attack {
        final Charge grip;
        final long started;
        final float damage;
        final int peakA, sound, side;
        final Set<Integer> hitA = new HashSet<>(), hitB = new HashSet<>();
        int peakB = -1, duration;
        boolean firstSound, secondSound;
        Attack(Charge grip, long started, float damage, int sound, int side) {
            this.grip = grip; this.started = started; this.damage = damage; this.sound = sound; this.side = side;
            peakA = WhipArmPose.CRACK;
            duration = WhipArmPose.LENGTH;
        }
    }
    private static final Map<UUID, Attack> ATTACKS = new HashMap<>();

    public static boolean holdsWhip(Player player) {
        return player.getMainHandItem().is(ChaosticItemRegistry.OWNERS_WHIP.get());
    }

    public static void handle(ServerPlayer player, byte action) {
        if (action == WhipActionPayload.CANCEL) {
            cancel(player);
            return;
        }
        if (!OwnersWhipItem.isOwner(player)) {
            if (holdsWhip(player) && action == WhipActionPayload.START) {
                player.displayClientMessage(Component.translatable("message.chaosticspells.whip_locked"), true);
            }
            return;
        }
        if (!player.isAlive() || player.isSpectator() || !holdsWhip(player)) {
            cancel(player);
            return;
        }
        if (action == WhipActionPayload.START) {
            Attack current = ATTACKS.get(player.getUUID());
            if (current != null) {
                int age = (int) (player.level().getGameTime() - current.started);
                if (valid(player, current.grip) && current.peakB < 0 && age > 0 && age <= current.peakA + 8) {
                    // The backhand starts once the first crack is out and replays the whole stroke.
                    int start = Math.max(current.peakA + 2, age + 1);
                    current.peakB = start + WhipArmPose.CRACK;
                    current.duration = start + WhipArmPose.LENGTH;
                    player.getCooldowns().addCooldown(ChaosticItemRegistry.OWNERS_WHIP.get(), current.duration-age+3);
                    motion(player, WhipMotionPayload.ATTACK, current.started, current.peakA, current.peakB, current.duration, current.side);
                }
                return;
            }
            if (CHARGES.containsKey(player.getUUID()) || player.getCooldowns().isOnCooldown(ChaosticItemRegistry.OWNERS_WHIP.get())) return;
            CHARGES.put(player.getUUID(), new Charge(player.getMainHandItem(), player.getInventory().selected,
                    player.level().dimension(), player.level().getGameTime()));
            motion(player, WhipMotionPayload.CHARGE, 0, -1, 0, 1);
        } else if (action == WhipActionPayload.RELEASE) {
            Charge charge = CHARGES.remove(player.getUUID());
            if (charge == null) return;
            if (!valid(player, charge) || player.getCooldowns().isOnCooldown(ChaosticItemRegistry.OWNERS_WHIP.get())) {
                motion(player, WhipMotionPayload.REST, 0, -1, 0, 1);
                return;
            }
            long elapsed = Math.max(0, player.level().getGameTime() - charge.started());
            float damage = OwnersWhipItem.damageForTicks(elapsed);
            boolean left = NEXT_LEFT.getOrDefault(player.getUUID(), true);
            NEXT_LEFT.put(player.getUUID(), !left);
            int sound = player.getRandom().nextInt(4), side = left ? 1 : -1;
            Attack attack = new Attack(charge, player.level().getGameTime(), damage, sound, side);
            ATTACKS.put(player.getUUID(), attack);
            player.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
            player.getCooldowns().addCooldown(ChaosticItemRegistry.OWNERS_WHIP.get(), attack.duration+3);
            motion(player, WhipMotionPayload.ATTACK, attack.started, attack.peakA, attack.peakB, attack.duration, side);
        }
    }

    private static boolean valid(ServerPlayer player, Charge charge) {
        return player.isAlive() && !player.isSpectator() && OwnersWhipItem.isOwner(player)
                && player.level().dimension().equals(charge.dimension())
                && player.getInventory().selected == charge.slot() && player.getMainHandItem() == charge.stack()
                && holdsWhip(player);
    }

    private static void motion(ServerPlayer player, byte kind, int peakA, int peakB, int duration, int side) {
        motion(player, kind, player.level().getGameTime(), peakA, peakB, duration, side);
    }

    private static void motion(ServerPlayer player, byte kind, long started, int peakA, int peakB, int duration, int side) {
        WhipVisuals.change(player,kind,started,peakA,peakB,duration,side);
    }

    public static void cancel(ServerPlayer player) {
        Charge charge = CHARGES.remove(player.getUUID());
        Attack attack = ATTACKS.remove(player.getUUID());
        if (charge != null || attack != null) motion(player, WhipMotionPayload.REST, 0, -1, 0, 1);
    }

    public static List<LivingEntity> findTargets(ServerPlayer player) {
        return WhipSweep.targets(player);
    }

    /** Compatibility helper for existing callers; combat resolves every target in the swept volume. */
    public static LivingEntity findTarget(ServerPlayer player) {
        return findTargets(player).stream().findFirst().orElse(null);
    }

    private static void sweep(ServerPlayer player, Attack attack, int age, int peak, int side, Set<Integer> hit) {
        if (peak < 0 || age < peak - 3 || age > peak + 3) return;
        double angle = (-WhipSweep.HALF_ARC_DEGREES + (age - peak + 3) * (2 * WhipSweep.HALF_ARC_DEGREES / 6)) * side;
        DamageSource source = null;
        for (LivingEntity target : WhipSweep.targets(player, angle, 24)) {
            if (!hit.add(target.getId())) continue;
            if (source == null) source = player.damageSources().source(DAMAGE_TYPE, player);
            strike(player, target, source, attack.damage);
        }
    }

    private static void strike(ServerPlayer player, LivingEntity target, DamageSource source, float damage) {
        if (!target.hurt(source, damage)) return;
        Vec3 direction = new Vec3(target.getX() - player.getX(), 0, target.getZ() - player.getZ());
        if (direction.lengthSqr() < 0.001) direction = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);
        // Golems and other heavy mobs keep their knockback resistance.
        double resist = 1 - Math.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0, 1);
        if (resist > 0) {
            direction = direction.normalize().scale((0.8 + 0.4 * (damage - 5) / 15) * resist);
            target.setDeltaMovement(direction.x, Math.max(target.getDeltaMovement().y, 0.25 * resist), direction.z);
            target.hurtMarked = true;
        }
        player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                target.getZ(), 8, 0.2, 0.2, 0.2, 0.1);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !OwnersWhipItem.isOwner(player)) return;
        Attack attack = ATTACKS.get(player.getUUID());
        if (attack != null) {
            if (!valid(player, attack.grip)) cancel(player);
            else {
                int age = (int) (player.level().getGameTime() - attack.started);
                if (!attack.firstSound && age >= attack.peakA-WhipSounds.peak(attack.sound)) {
                    attack.firstSound = true;
                    player.serverLevel().playSound(null, player.getX(), player.getEyeY(), player.getZ(), WhipSounds.single(attack.sound), SoundSource.PLAYERS, .9f, 1f);
                }
                if (attack.peakB >= 0 && !attack.secondSound && age >= attack.peakB-WhipSounds.FOLLOW_UP_PEAK) {
                    attack.secondSound = true;
                    player.serverLevel().playSound(null, player.getX(), player.getEyeY(), player.getZ(), WhipSounds.FOLLOW_UP.get(), SoundSource.PLAYERS, .9f, 1f);
                }
                sweep(player, attack, age, attack.peakA, attack.side, attack.hitA);
                sweep(player, attack, age, attack.peakB, -attack.side, attack.hitB);
                if (age >= attack.duration) {
                    ATTACKS.remove(player.getUUID());
                    motion(player, WhipMotionPayload.REST, 0, -1, 0, 1);
                }
            }
        }
        Charge charge = CHARGES.get(player.getUUID());
        if (charge != null) {
            if (!valid(player, charge)) cancel(player);
            else if (player.tickCount % 4 == 0) {
                long elapsed = Math.max(0, player.level().getGameTime() - charge.started());
                player.displayClientMessage(Component.translatable("message.chaosticspells.whip_charge",
                        Math.min(100, elapsed * 100 / OwnersWhipItem.MAX_CHARGE_TICKS),
                        String.format(java.util.Locale.ROOT, "%.1f", OwnersWhipItem.damageForTicks(elapsed))), true);
            }
        }
        WhipVisuals.tick(player);
    }

    @SubscribeEvent
    public static void blockVanillaAttack(AttackEntityEvent event) {
        if (holdsWhip(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void blockMining(PlayerInteractEvent.LeftClickBlock event) {
        if (holdsWhip(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { cancel(player); WhipVisuals.clear(player); }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        CHARGES.remove(event.getEntity().getUUID());
        ATTACKS.remove(event.getEntity().getUUID());
        NEXT_LEFT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        CHARGES.clear();
        ATTACKS.clear();
        NEXT_LEFT.clear();
    }

    private WhipCombat() {}
}

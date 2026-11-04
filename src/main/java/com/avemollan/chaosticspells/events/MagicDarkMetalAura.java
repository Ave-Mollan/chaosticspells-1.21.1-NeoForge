package com.avemollan.chaosticspells.events;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

import com.avemollan.chaosticspells.registry.ChaosticParticleRegistry;

/**
 * Chaos glyphs around a player while the Magic Dark Metal full-set effect is acting on them: the low-health bonus
 * (health at or below 30% with the full set) applies its effect for {@link #EFFECT_TICKS}; the glyphs last as long as
 * that effect does (and stop early if it is removed, e.g. by milk) and only while the full set is worn.
 *
 * <p>Server-driven because clients only know their own mob effects; {@code sendParticles} with count 0 reaches every
 * player within 32 blocks, and the velocity slots carry the orbit centre and rise speed (see ChaosGlyphParticle).
 */
public final class MagicDarkMetalAura {
    public static final int EFFECT_TICKS = 100;
    /** Average glyphs per tick (about 9 per second). */
    private static final float SPAWN_CHANCE = 0.45f;

    private record Activation(long untilGameTime, Holder<MobEffect> effect) {}

    private static final Map<Player, Activation> ACTIVE = new WeakHashMap<>();

    private MagicDarkMetalAura() {}

    /** Called by the full-set tick each time the low-health effect is (re)applied. */
    public static void markTriggered(Player player, Holder<MobEffect> effect) {
        ACTIVE.put(player, new Activation(player.level().getGameTime() + EFFECT_TICKS, effect));
    }

    public static boolean isActive(Player player) {
        Activation activation = ACTIVE.get(player);
        if (activation == null) {
            return false;
        }
        if (player.level().getGameTime() >= activation.untilGameTime() || !player.hasEffect(activation.effect())) {
            ACTIVE.remove(player);
            return false;
        }
        return true;
    }

    /** Called every server tick while the full set is worn. */
    public static void tick(Player player) {
        if (!(player.level() instanceof ServerLevel level) || !isActive(player)
                || player.isInvisible() || player.isSpectator()) {
            return;
        }
        RandomSource random = player.getRandom();
        if (random.nextFloat() >= SPAWN_CHANCE) {
            return;
        }
        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = 0.6 + random.nextDouble() * 0.35;
        double x = player.getX() + Math.cos(angle) * radius;
        double z = player.getZ() + Math.sin(angle) * radius;
        double y = player.getY() + 0.1 + random.nextDouble() * player.getBbHeight();
        double rise = 0.01 + random.nextDouble() * 0.015;
        level.sendParticles(ChaosticParticleRegistry.CHAOS_GLYPH.get(), x, y, z, 0, player.getX(), rise, player.getZ(), 1.0);
    }
}

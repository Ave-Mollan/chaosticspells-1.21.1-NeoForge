package com.avemollan.chaosticspells.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class DeathPactEffect extends MagicMobEffect {

    public DeathPactEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity.level().isClientSide && livingEntity.tickCount % 40 == 0) {
            livingEntity.level().playLocalSound(
                    livingEntity.getX(),
                    livingEntity.getY(),
                    livingEntity.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT,
                    SoundSource.PLAYERS,
                    0.3f,
                    0.9f,
                    false);
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}

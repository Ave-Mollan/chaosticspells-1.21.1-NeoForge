package com.avemollan.chaosticspells.entity;

import com.avemollan.chaosticspells.util.ChaoticArrowEffects;
import com.avemollan.chaosticspells.registry.ChaosticEntityRegistry;
import com.avemollan.chaosticspells.registry.ChaosticSpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

public class ChaoticArrowProjectile extends AbstractMagicProjectile {
    private int spellLevel = 1;

    public ChaoticArrowProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public ChaoticArrowProjectile(Level level, LivingEntity shooter) {
        this(ChaosticEntityRegistry.CHAOTIC_ARROW.get(), level);
        this.setOwner(shooter);
    }

    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }

    @Override
    public void trailParticles() {
        if (!this.level().isClientSide || this.random.nextInt(2) != 0) {
            return;
        }
        ChaoticArrowEffects.spawnFlightParticle(this);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 2.5f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(Holder.direct(SoundEvents.ARROW_HIT));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level().isClientSide) {
            return;
        }
        DamageSources.applyDamage(
                entityHitResult.getEntity(),
                this.damage,
                ChaosticSpellRegistry.CHAOTIC_ARROW.get().getDamageSource(this, getOwner()));
        if (entityHitResult.getEntity() instanceof LivingEntity living) {
            living.setArrowCount(living.getArrowCount() + 1);
            ChaoticArrowEffects.applyRandomChaoticEffect(living, this.spellLevel);
        }
        this.discard();
    }
}

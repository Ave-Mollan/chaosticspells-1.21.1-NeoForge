package com.avemollan.chaosticspells.entity;

import com.avemollan.chaosticspells.registry.ChaosticEntityRegistry;
import com.avemollan.chaosticspells.registry.ChaosticSpellRegistry;
import com.avemollan.chaosticspells.util.BornInChaosEffects;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

public class MagicArrowProjectile extends AbstractMagicProjectile {

    public MagicArrowProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public MagicArrowProjectile(Level level, LivingEntity shooter) {
        this(ChaosticEntityRegistry.MAGIC_ARROW.get(), level);
        this.setOwner(shooter);
    }

    @Override
    public void trailParticles() {
        BornInChaosEffects.addMagicTrailClient(this.level(), this.getX(), this.getY(), this.getZ());
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 3.0f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level().isClientSide) {
            return;
        }
        var target = entityHitResult.getEntity();
        DamageSources.applyDamage(
                target,
                this.damage,
                ChaosticSpellRegistry.MAGIC_ARROW.get().getDamageSource(this, getOwner()));
        if (this.level() instanceof ServerLevel serverLevel) {
            BornInChaosEffects.sendMagichit(serverLevel, target);
        }
        this.discard();
    }
}

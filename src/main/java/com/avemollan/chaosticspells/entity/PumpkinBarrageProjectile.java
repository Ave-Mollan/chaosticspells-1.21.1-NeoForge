package com.avemollan.chaosticspells.entity;

import com.avemollan.chaosticspells.registry.ChaosticEntityRegistry;
import com.avemollan.chaosticspells.registry.ChaosticSpellRegistry;
import com.avemollan.chaosticspells.util.BornInChaosEffects;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import java.util.Optional;

public class PumpkinBarrageProjectile extends AbstractMagicProjectile {

    private static final String BORN_IN_CHAOS_ID = "born_in_chaos_v1";

    public PumpkinBarrageProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public PumpkinBarrageProjectile(Level level, LivingEntity shooter) {
        this(ChaosticEntityRegistry.PUMPKIN_BARRAGE_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setCursorHoming(true);
    }

    private LivingEntity ownerLiving() {
        return this.getOwner() instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && ModList.get().isLoaded(BORN_IN_CHAOS_ID) && this.level() instanceof ServerLevel serverLevel) {
            BornInChaosEffects.sendPumpkinStaffFlightTrail(serverLevel, this.getX(), this.getY(), this.getZ());
        }
    }

    @Override
    public void trailParticles() {
        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        if (this.tickCount <= 2 || this.random.nextInt(3) != 0) {
            return;
        }
        this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        boolean lordHatExplosion = BornInChaosEffects.isLordPumpkinHatEquipped(ownerLiving());
        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            BornInChaosEffects.sendPumpkinStaffImpact(serverLevel, x, y, z, lordHatExplosion);
            return;
        }
        int flames = lordHatExplosion ? 18 : 12;
        serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, flames, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    @Override
    public float getSpeed() {
        return 1.5f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return Optional.empty();
        }
        return Optional.of(Holder.direct(SoundEvents.WOOD_BREAK));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            var pos = result.getBlockPos();
            Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
            BornInChaosEffects.applyPumpkinBarrageExplosionAoe(serverLevel, this, ownerLiving(), center);
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (this.level().isClientSide) {
            return;
        }
        var entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.invulnerableTime = 0;
        }
        DamageSources.applyDamage(
                entity,
                this.damage,
                ChaosticSpellRegistry.PUMPKIN_BARRAGE.get().getDamageSource(this, getOwner()));
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setArrowCount(livingEntity.getArrowCount() + 1);
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 center = new Vec3(entity.getX(), entity.getY() + 1.0D, entity.getZ());
            BornInChaosEffects.applyPumpkinBarrageExplosionAoe(serverLevel, this, ownerLiving(), center);
        }
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.invulnerableTime = 1;
        }
        this.discard();
    }
}

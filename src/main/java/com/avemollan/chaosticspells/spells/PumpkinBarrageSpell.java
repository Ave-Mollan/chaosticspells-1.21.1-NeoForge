package com.avemollan.chaosticspells.spells;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.entity.PumpkinBarrageProjectile;
import com.avemollan.chaosticspells.registry.ChaosticSchoolRegistry;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class PumpkinBarrageSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "pumpkin_barrage");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(ChaosticSchoolRegistry.CHAOS_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public PumpkinBarrageSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", getProjectileCount(spellLevel))
        );
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BLAZE_SHOOT);
    }

    private static int getProjectileCount(int spellLevel) {
        return Math.min(3 + (spellLevel / 2), 7);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int projectileCount = getProjectileCount(spellLevel);
        float damage = getDamage(spellLevel, entity);

        Vec3 origin = entity.getEyePosition();
        float angleStep = 360.0f / projectileCount;
        float radius = 2.0f;

        for (int i = 0; i < projectileCount; i++) {
            float angle = angleStep * i;
            float angleRad = (float) Math.toRadians(angle);

            double offsetX = Math.cos(angleRad) * radius;
            double offsetZ = Math.sin(angleRad) * radius;

            PumpkinBarrageProjectile projectile = new PumpkinBarrageProjectile(world, entity);
            Vec3 spawnPos = origin.add(offsetX, 0.5, offsetZ);
            projectile.setPos(spawnPos);

            Vec3 sidewaysDirection = new Vec3(offsetX, 0, offsetZ).normalize();
            double upwardSpeed = 1.0;
            double sidewaysSpeed = 0.2;
            Vec3 velocity = sidewaysDirection.scale(sidewaysSpeed).add(0, upwardSpeed, 0);
            velocity = velocity.add(
                    (world.random.nextDouble() - 0.5) * 0.05,
                    (world.random.nextDouble() - 0.5) * 0.1,
                    (world.random.nextDouble() - 0.5) * 0.05
            );
            projectile.setDeltaMovement(velocity);
            projectile.setDamage(damage);

            world.addFreshEntity(projectile);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }
}

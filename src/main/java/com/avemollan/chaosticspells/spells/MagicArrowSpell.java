package com.avemollan.chaosticspells.spells;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.entity.MagicArrowProjectile;
import com.avemollan.chaosticspells.registry.ChaosticSchoolRegistry;
import com.avemollan.chaosticspells.util.BornInChaosEffects;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class MagicArrowSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "magic_arrow");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(ChaosticSchoolRegistry.CHAOS_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(1)
            .build();

    public MagicArrowSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)));
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
        return Optional.of(SoundEvents.ARROW_SHOOT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        MagicArrowProjectile arrow = new MagicArrowProjectile(world, entity);
        arrow.setPos(entity.position()
                .add(0, entity.getEyeHeight() - arrow.getBoundingBox().getYsize() * 0.5f, 0)
                .add(entity.getForward()));
        arrow.shoot(entity.getLookAngle());
        arrow.setDamage(getDamage(spellLevel, entity));
        world.addFreshEntity(arrow);

        if (world instanceof ServerLevel serverLevel) {
            BornInChaosEffects.sendRitualParticles(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        }
        BornInChaosEffects.playMagicStaffShoot(world, entity);

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }
}

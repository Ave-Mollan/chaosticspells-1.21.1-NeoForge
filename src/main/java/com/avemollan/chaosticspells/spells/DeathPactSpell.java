package com.avemollan.chaosticspells.spells;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.registry.ChaosticMobEffectRegistry;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class DeathPactSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "death_pact");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(ChaosticSchoolRegistry.CHAOS_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(180)
            .build();

    public DeathPactSpell() {
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 200;
        this.spellPowerPerLevel = 120;
        this.castTime = 0;
        this.baseManaCost = 100;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.effect_length",
                Utils.timeFromTicks(getSpellPower(spellLevel, caster), 1)));
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
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int duration = (int) getSpellPower(spellLevel, entity);
        int amplifier = spellLevel - 1;
        entity.addEffect(new MobEffectInstance(
                ChaosticMobEffectRegistry.DEATH_PACT,
                duration,
                Math.max(0, amplifier),
                false,
                false,
                true));
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}

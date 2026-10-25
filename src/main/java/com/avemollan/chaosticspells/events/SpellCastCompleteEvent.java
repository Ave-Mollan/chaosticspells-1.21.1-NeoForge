package com.avemollan.chaosticspells.events;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public final class SpellCastCompleteEvent extends Event {

    private final Level level;
    private final int spellLevel;
    private final LivingEntity entity;
    private final MagicData playerMagicData;
    private final boolean cancelled;
    private final AbstractSpell spell;

    public SpellCastCompleteEvent(
            Level level,
            int spellLevel,
            LivingEntity entity,
            MagicData playerMagicData,
            boolean cancelled,
            AbstractSpell spell
    ) {
        this.level = level;
        this.spellLevel = spellLevel;
        this.entity = entity;
        this.playerMagicData = playerMagicData;
        this.cancelled = cancelled;
        this.spell = spell;
    }

    public Level getLevel() {
        return level;
    }

    public int getSpellLevel() {
        return spellLevel;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public MagicData getPlayerMagicData() {
        return playerMagicData;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public AbstractSpell getSpell() {
        return spell;
    }
}

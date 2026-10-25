package com.avemollan.chaosticspells.mixin;

import com.avemollan.chaosticspells.events.SpellCastCompleteEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSpell.class)
public abstract class AbstractSpellMixin {

    @Inject(method = "onServerCastComplete", at = @At("HEAD"))
    private void chaosticspells$postCastComplete(
            Level level,
            int spellLevel,
            LivingEntity entity,
            MagicData playerMagicData,
            boolean cancelled,
            CallbackInfo ci
    ) {
        NeoForge.EVENT_BUS.post(
                new SpellCastCompleteEvent(level, spellLevel, entity, playerMagicData, cancelled, (AbstractSpell) (Object) this));
    }
}

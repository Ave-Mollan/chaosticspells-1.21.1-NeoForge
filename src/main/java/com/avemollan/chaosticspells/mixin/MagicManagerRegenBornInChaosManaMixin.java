package com.avemollan.chaosticspells.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerPlayer;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;

import com.avemollan.chaosticspells.util.BornInChaosEffects;

/**
 * Scales passive Iron's Spellbooks mana gain in {@link MagicManager#regenPlayerMana}; see Born in Chaos compat rates
 * in {@link BornInChaosEffects#getIronSpellbooksPassiveManaRegenMultiplier}.
 */
@Mixin(MagicManager.class)
public class MagicManagerRegenBornInChaosManaMixin {

    @WrapOperation(
            method =
                    "regenPlayerMana(Lnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/magic/MagicData;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float chaosticspells$scalePassiveManaRegenForBornInChaos(
            float proposedMana,
            float minimum,
            float maximum,
            Operation<Float> original,
            @Local(argsOnly = true) MagicData magicData,
            @Local(argsOnly = true, ordinal = 0) ServerPlayer serverPlayer) {
        float previous = magicData.getMana();
        float gain = proposedMana - previous;
        float mult = BornInChaosEffects.getIronSpellbooksPassiveManaRegenMultiplier(serverPlayer);
        if (gain > 0.0f && mult != 1.0f) {
            proposedMana = previous + gain * mult;
        }
        // Scale the actual increment before Iron's clamps it to the player's mana capacity.
        return original.call(proposedMana, minimum, maximum);
    }
}

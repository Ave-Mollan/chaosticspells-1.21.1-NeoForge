package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ViltrumiteCore adds {@code public boolean isBlocking()} to {@code Player} for its own block ability; that method
 * overrides {@link LivingEntity#isBlocking()}, so for players vanilla only sees the Viltrumite flag and a raised shield
 * never blocks (our race lock even forces the flag to false for non-Viltrumites). Where vanilla decides on shield
 * blocking, a raised shield counts again; Viltrumite's own callers keep seeing only its flag.
 */
@Mixin(LivingEntity.class)
public abstract class ShieldBlockingFixMixin {
    @WrapOperation(method = "isDamageSourceBlocked",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isBlocking()Z"))
    private boolean chaosticspells$raisedShieldBlocks(LivingEntity self, Operation<Boolean> original) {
        return chaosticspells$raisingShield(self) || original.call(self);
    }

    /** {@link LivingEntity#isBlocking()} as written in NeoForge 21.1 (the body a player can no longer reach). */
    private static boolean chaosticspells$raisingShield(LivingEntity entity) {
        if (!entity.isUsingItem()) return false;
        ItemStack stack = entity.getUseItem();
        return !stack.isEmpty() && stack.canPerformAction(ItemAbilities.SHIELD_BLOCK)
                && stack.getItem().getUseDuration(stack, entity) - entity.getUseItemRemainingTicks() >= 5;
    }
}

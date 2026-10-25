package com.avemollan.chaosticspells.mixin.compat.bicnightmare;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.avemollan.chaosticspells.compat.BornInChaosNightmareArmorCompatBridge;

/** Nightmare Mantle chestplate full-set tick from BiC — also covers our chest when combined with other mixins. */
@Mixin(targets = "net.mcreator.borninchaosv.procedures.NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure")
public class BcNightmareCompatMixin_Entity {

    @ModifyExpressionValue(
            method = "execute(Lnet/minecraft/world/entity/Entity;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private static Item unwrap(Item worn) {
        return BornInChaosNightmareArmorCompatBridge.unwrapForProcedure(worn);
    }
}

package com.avemollan.chaosticspells.mixin.compat.bicnightmare;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.avemollan.chaosticspells.compat.BornInChaosNightmareArmorCompatBridge;

@Mixin(targets = "net.mcreator.borninchaosv.procedures.NightmareScytheKoghdaZhivaiaSushchnostPopadaietSPomoshchiuInstrumientaProcedure")
public class BcNightmareCompatMixin_Entity_Entity_A {

    @ModifyExpressionValue(
            method =
                    "execute(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private static Item unwrap(Item worn) {
        return BornInChaosNightmareArmorCompatBridge.unwrapForProcedure(worn);
    }
}

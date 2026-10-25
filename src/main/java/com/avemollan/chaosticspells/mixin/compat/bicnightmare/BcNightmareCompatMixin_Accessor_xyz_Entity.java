package com.avemollan.chaosticspells.mixin.compat.bicnightmare;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.avemollan.chaosticspells.compat.BornInChaosNightmareArmorCompatBridge;

/** Pumpkin pistol projectile hit (BiC 1.7.6: single public {@code execute(world, x, y, z, entity)}). */
@Mixin(targets = "net.mcreator.borninchaosv.procedures.PumpkinPistolKoghdaSnariadPopadaietVSushchnostProcedure")
public class BcNightmareCompatMixin_Accessor_xyz_Entity {

    @ModifyExpressionValue(
            method =
                    "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private static Item unwrap(Item worn) {
        return BornInChaosNightmareArmorCompatBridge.unwrapForProcedure(worn);
    }
}

package com.avemollan.chaosticspells.mixin.compat.bicnightmare;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.avemollan.chaosticspells.compat.BornInChaosNightmareArmorCompatBridge;

/**
 * Dark Ritual Dagger right-click. Since BiC 1.7.x the public {@code execute(world, x, y, z, entity, sourceentity)}
 * only delegates to a private {@code execute(Event, ...)} that holds the nightmare-set {@code getItem()} checks.
 */
@Mixin(targets = "net.mcreator.borninchaosv.procedures.DarkRitualDaggerPProcedure")
public class BcNightmareCompatMixin_Accessor_xyz_Entity_Entity {

    @ModifyExpressionValue(
            method =
                    "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private static Item unwrap(Item worn) {
        return BornInChaosNightmareArmorCompatBridge.unwrapForProcedure(worn);
    }
}

package com.avemollan.chaosticspells.mixin.compat.bicnightmare;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.avemollan.chaosticspells.compat.BornInChaosNightmareArmorCompatBridge;

/** Staff of Summoner click, Staff of Magic Arrows use, Frostbitten Blade RMB. */
@Mixin(
        targets = {
            "net.mcreator.borninchaosv.procedures.StaffoftheSummonerPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure",
            "net.mcreator.borninchaosv.procedures.StaffofMagicArrowsPriIspolzovaniiStrielkovoghoPriedmietaProcedure",
            "net.mcreator.borninchaosv.procedures.FrostbittenBladePriShchielchkiePKMProcedure"
        })
public class BcNightmareCompatMixin_Accessor_xyz_Entity_ItemStack {

    @ModifyExpressionValue(
            method =
                    "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private static Item unwrap(Item worn) {
        return BornInChaosNightmareArmorCompatBridge.unwrapForProcedure(worn);
    }
}

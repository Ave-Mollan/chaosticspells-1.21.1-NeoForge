package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.baranhan.viltrumitecore.client.AbilityInputManager", remap = false)
public abstract class ClientAbilityInputMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void ave$noHumanMenus(Minecraft client, CallbackInfo ci) {
        if (!AveViltrumiteAccess.isActive(client.player)) ci.cancel();
    }

    @Inject(method = {"consumeAbilityKeyPress", "isAbilityKeyDown"}, at = @At("HEAD"), cancellable = true)
    private static void ave$noHumanKeys(Player player, String ability, CallbackInfoReturnable<Boolean> cir) {
        if (!AveViltrumiteAccess.isActive(player)) cir.setReturnValue(false);
    }
}

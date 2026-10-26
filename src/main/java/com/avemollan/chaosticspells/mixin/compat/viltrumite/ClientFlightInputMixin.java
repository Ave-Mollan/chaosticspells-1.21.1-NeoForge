package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.baranhan.viltrumiteflight.client.FlightInputHandler", remap = false)
public abstract class ClientFlightInputMixin {
    @Shadow private static boolean wasJumpPressed;
    @Shadow private static long lastJumpTime;
    @Shadow private static boolean wasAccelerating;
    @Shadow private static boolean wasSneakPressed;

    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true)
    private static void ave$normalDoubleJump(ClientTickEvent.Post event, CallbackInfo ci) {
        if (!AveViltrumiteAccess.isActive(Minecraft.getInstance().player)) {
            wasJumpPressed = false;
            lastJumpTime = 0;
            wasAccelerating = false;
            wasSneakPressed = false;
            ci.cancel();
        }
    }
}

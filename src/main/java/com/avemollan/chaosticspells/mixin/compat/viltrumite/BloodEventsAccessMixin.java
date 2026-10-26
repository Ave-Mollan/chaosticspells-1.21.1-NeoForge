package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.baranhan.viltrumitecore.event.BloodEvents", remap = false)
public abstract class BloodEventsAccessMixin {
    @Inject(method = "onLivingDamage", at = @At("HEAD"), cancellable = true)
    private static void ave$noUnrelatedBlood(LivingDamageEvent.Post event, CallbackInfo ci) {
        if (!AveViltrumiteAccess.isActive(event.getEntity()) && !AveViltrumiteAccess.isActive(event.getSource().getEntity())) ci.cancel();
    }
}

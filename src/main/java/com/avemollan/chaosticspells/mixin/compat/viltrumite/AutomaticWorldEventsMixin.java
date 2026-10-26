package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Automatic raids and meteors must not reveal the integration to unrelated players. */
@Mixin(targets = {"dev.baranhan.viltrumitecore.event.CoreEvents", "dev.baranhan.viltrumitecore.event.MeteorSpawnEvent"}, remap = false)
public abstract class AutomaticWorldEventsMixin {
    @Inject(method = "onLevelTick", at = @At("HEAD"), cancellable = true)
    private static void ave$noGlobalEvents(LevelTickEvent.Post event, CallbackInfo ci) {
        ci.cancel();
    }
}

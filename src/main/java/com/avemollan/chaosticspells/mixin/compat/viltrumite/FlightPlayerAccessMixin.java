package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess;
import dev.baranhan.viltrumiteflight.util.FlightState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 0)
public abstract class FlightPlayerAccessMixin {
    @Dynamic("Added to Player by ViltrumiteFlight")
    @Inject(method = "getFlightState", at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$normalFlight(CallbackInfoReturnable<FlightState> cir) {
        if (!AveViltrumiteAccess.isActive((Player) (Object) this)) cir.setReturnValue(FlightState.NONE);
    }

    @Dynamic("Added to Player by ViltrumiteFlight")
    @ModifyVariable(method = "setFlightState", at = @At("HEAD"), argsOnly = true, remap = false)
    private FlightState ave$authorizeFlight(FlightState requested) {
        return AveViltrumiteAccess.isActive((Player) (Object) this) ? requested : FlightState.NONE;
    }

    @Dynamic("Added to Player by ViltrumiteFlight")
    @Inject(method = {"getFlightThrottle", "getLerpedFlightThrottle"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$noHumanThrottle(CallbackInfoReturnable<Float> cir) {
        if (!AveViltrumiteAccess.isActive((Player) (Object) this)) cir.setReturnValue(0f);
    }
}

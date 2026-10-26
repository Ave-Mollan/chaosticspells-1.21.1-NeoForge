package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.avemollan.chaosticspells.compat.viltrumite.AveRaceState;
import com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess;
import dev.baranhan.viltrumitecore.util.ViltrumiteCorePlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 0)
public abstract class CorePlayerAccessMixin implements AveRaceState {
    @Override
    public boolean chaosticspells$isViltrumite() {
        return ((ViltrumiteCorePlayer) this).isViltrumite();
    }

    @Dynamic("Added to Player by ViltrumiteCore")
    @Inject(method = "isViltrumite", at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$race(CallbackInfoReturnable<Boolean> cir) {
        if (!AveViltrumiteAccess.mayHaveRace((Player) (Object) this)) cir.setReturnValue(false);
    }

    @Dynamic("Added to Player by ViltrumiteCore")
    @ModifyVariable(method = "setViltrumite", at = @At("HEAD"), argsOnly = true, remap = false)
    private boolean ave$authorizeRace(boolean requested) {
        Player player = (Player) (Object) this;
        return player.level().isClientSide() ? requested && AveViltrumiteAccess.isOwner(player) : AveViltrumiteAccess.selected(player);
    }

    @Dynamic("Added to Player by ViltrumiteCore")
    @Inject(method = "hasChosenRace", at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$noRacePrompt(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Dynamic("Added to Player by ViltrumiteCore")
    @Inject(method = {"isDashing", "isBlocking", "isSuperSpeed", "isTryingToGrab", "isBarraging", "isChargingPunch"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$noHumanActions(CallbackInfoReturnable<Boolean> cir) {
        if (!AveViltrumiteAccess.isActive((Player) (Object) this)) cir.setReturnValue(false);
    }

    @Dynamic("Added to Player by ViltrumiteCore")
    @Inject(method = {"getPunchTicks", "getChopTicks", "getThunderclapTicks", "getBarrageTicks", "getChargePunchTicks"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$noHumanAnimations(CallbackInfoReturnable<Integer> cir) {
        if (!AveViltrumiteAccess.isActive((Player) (Object) this)) cir.setReturnValue(0);
    }

    @Dynamic("Added to Player by ViltrumiteCore")
    @Inject(method = {"getViltrumiteSkin", "getViltrumiteCape"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$noHumanCosmetics(CallbackInfoReturnable<String> cir) {
        if (!AveViltrumiteAccess.isActive((Player) (Object) this)) cir.setReturnValue("off");
    }

    @Dynamic("Added to Player by ViltrumiteCore")
    @Inject(method = "getViltrumiteModel", at = @At("HEAD"), cancellable = true, remap = false)
    private void ave$normalHumanModel(CallbackInfoReturnable<String> cir) {
        if (!AveViltrumiteAccess.isActive((Player) (Object) this)) cir.setReturnValue("default");
    }
}

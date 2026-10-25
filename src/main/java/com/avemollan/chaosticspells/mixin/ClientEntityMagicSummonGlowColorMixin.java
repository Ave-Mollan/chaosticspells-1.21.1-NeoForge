package com.avemollan.chaosticspells.mixin;

import com.avemollan.chaosticspells.entity.RaisedBabySkeleton;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mirrors {@link io.redspace.ironsspellbooks.mixin.ClientEntityMixin} for summons not backed by spell recasts
 * (e.g. RaisedBabySkeleton from the missionary hat), using synced owner UUID on the entity.
 */
@Mixin(Entity.class)
public abstract class ClientEntityMagicSummonGlowColorMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void chaosticspells$raisedBabyGlowColor(CallbackInfoReturnable<Integer> cir) {
        var entity = ((Entity) (Object) this);
        if (!(entity instanceof RaisedBabySkeleton summon)) {
            return;
        }
        if (!ClientConfigs.SUMMONS_GLOW.get()) {
            return;
        }
        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Entity owner = summon.getSummoner();
        if (owner != null && owner.getUUID().equals(mc.player.getUUID())) {
            cir.setReturnValue(ClientConfigs.summonGlowColor);
        }
    }
}

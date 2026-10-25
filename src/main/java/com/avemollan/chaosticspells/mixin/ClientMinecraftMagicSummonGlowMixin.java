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
 * Mirrors {@link io.redspace.ironsspellbooks.mixin.MinecraftMixin} summon glow outline for {@link RaisedBabySkeleton}
 * when Iron's Spellbooks summon glow setting is enabled.
 */
@Mixin(Minecraft.class)
public abstract class ClientMinecraftMagicSummonGlowMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
    private void chaosticspells$raisedBabyShouldGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player == null || entity == null || cir.getReturnValue()) {
            return;
        }
        if (!ClientConfigs.SUMMONS_GLOW.get()) {
            return;
        }
        if (entity instanceof RaisedBabySkeleton summon) {
            Entity owner = summon.getSummoner();
            if (owner != null && owner.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
                cir.setReturnValue(true);
            }
        }
    }
}

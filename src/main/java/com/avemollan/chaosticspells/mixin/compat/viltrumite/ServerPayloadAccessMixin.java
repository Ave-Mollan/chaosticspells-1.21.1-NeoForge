package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerPayloadAccessMixin {
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void ave$denyUnauthorizedActions(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if ((Object) this instanceof ServerGamePacketListenerImpl listener
                && AveViltrumiteAccess.isViltrumiteNamespace(packet.payload().type().id().getNamespace())) {
            // Read the saved opt-in and entity state on the server thread, never on a Netty thread.
            PacketUtils.ensureRunningOnSameThread(packet, listener, listener.player.serverLevel());
            if (!AveViltrumiteAccess.allowPayload(listener.player, packet.payload().type().id())) ci.cancel();
        }
    }
}

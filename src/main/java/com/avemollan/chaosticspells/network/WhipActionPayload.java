package com.avemollan.chaosticspells.network;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.events.WhipCombat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** The client sends only button transitions; target, duration and damage are decided by the server. */
public record WhipActionPayload(byte action) implements CustomPacketPayload {
    public static final byte START = 0, RELEASE = 1, CANCEL = 2;
    public static final Type<WhipActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "whip_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WhipActionPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BYTE, WhipActionPayload::action, WhipActionPayload::new);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("whip-1").playToServer(TYPE, CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                WhipCombat.handle(player, payload.action());
            }
        });
    }

    @Override
    public Type<WhipActionPayload> type() {
        return TYPE;
    }
}

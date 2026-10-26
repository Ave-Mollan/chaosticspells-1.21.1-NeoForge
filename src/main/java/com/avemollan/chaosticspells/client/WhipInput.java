package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.events.WhipCombat;
import com.avemollan.chaosticspells.network.WhipActionPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ChaosticSpells.MODID, value = Dist.CLIENT)
public final class WhipInput {
    private static boolean charging, pressLatched;

    private static void press(Minecraft client) {
        if (pressLatched || charging) return;
        pressLatched = true;
        if (WhipClientMotion.canQueueFollowUp()) {
            PacketDistributor.sendToServer(new WhipActionPayload(WhipActionPayload.START));
        } else if (!client.player.getCooldowns().isOnCooldown(client.player.getMainHandItem().getItem())) {
            PacketDistributor.sendToServer(new WhipActionPayload(WhipActionPayload.START));
            charging = true;
        }
    }

    @SubscribeEvent
    public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !WhipCombat.holdsWhip(client.player) || !event.isAttack()) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if (client.screen == null && client.isWindowActive()) press(client);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) {
            charging = pressLatched = false;
            return;
        }
        boolean equipped = client.screen == null && client.isWindowActive() && client.player.isAlive()
                && !client.player.isSpectator() && WhipCombat.holdsWhip(client.player);
        boolean down = equipped && client.options.keyAttack.isDown();
        if (down) press(client);
        else {
            if (charging) PacketDistributor.sendToServer(new WhipActionPayload(equipped ? WhipActionPayload.RELEASE : WhipActionPayload.CANCEL));
            charging = false;
            pressLatched = false;
        }
    }

    private WhipInput() {}
}

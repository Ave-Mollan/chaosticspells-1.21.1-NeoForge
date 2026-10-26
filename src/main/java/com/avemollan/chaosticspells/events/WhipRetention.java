package com.avemollan.chaosticspells.events;

import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Save the actual death drops, including components, rather than issuing a new whip on every respawn. */
public final class WhipRetention {
    private static final String SAVED = "chaosticspells:retained_whips";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !OwnersWhipItem.isOwner(player)) return;
        WhipCombat.cancel(player);
        ListTag saved = player.getPersistentData().getList(SAVED, Tag.TAG_COMPOUND);
        event.getDrops().removeIf(drop -> {
            ItemStack stack = drop.getItem();
            if (!stack.is(ChaosticItemRegistry.OWNERS_WHIP.get())) return false;
            saved.add(stack.save(player.registryAccess()));
            return true;
        });
        if (!saved.isEmpty()) player.getPersistentData().put(SAVED, saved);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !OwnersWhipItem.isOwner(event.getEntity())) return;
        var original = event.getOriginal().getPersistentData();
        if (original.contains(SAVED, Tag.TAG_LIST)) {
            event.getEntity().getPersistentData().put(SAVED, original.getList(SAVED, Tag.TAG_COMPOUND).copy());
            original.remove(SAVED);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player);
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) restore(player);
    }

    public static void restore(ServerPlayer player) {
        if (!player.isAlive() || !OwnersWhipItem.isOwner(player) || !player.getPersistentData().contains(SAVED)) return;
        ListTag pending = new ListTag();
        for (Tag tag : player.getPersistentData().getList(SAVED, Tag.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.parse(player.registryAccess(), tag).orElse(ItemStack.EMPTY);
            if (!stack.is(ChaosticItemRegistry.OWNERS_WHIP.get())) continue;
            // A full inventory must never cause a protected item to be dropped or lost.
            player.getInventory().add(stack);
            if (!stack.isEmpty()) pending.add(stack.save(player.registryAccess()));
        }
        if (pending.isEmpty()) player.getPersistentData().remove(SAVED);
        else player.getPersistentData().put(SAVED, pending);
        player.inventoryMenu.broadcastChanges();
    }

    private WhipRetention() {}
}

package com.avemollan.chaosticspells.commands;

import com.avemollan.chaosticspells.registry.ChaosticMenuRegistry;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Live view of another player's inventory for /ave inventory. Slots cover every container index of the target's
 * Inventory (main, armor, offhand and compartments added by other mods) and borrow placement rules, stack limits and
 * empty-slot icons from the target's own inventory screen, so modded slot restrictions keep working.
 */
public final class AveInventoryMenu extends AbstractContainerMenu {
    private static final SimpleCommandExceptionType SELF = new SimpleCommandExceptionType(
            Component.translatable("commands.chaosticspells.ave_inventory.self"));

    /** Compartment sizes in the target Inventory's container index order; anything after them was added by a mod. */
    public record Layout(int items, int armor, int offhand, int total) {
        static final StreamCodec<ByteBuf, Layout> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Layout::items, ByteBufCodecs.VAR_INT, Layout::armor,
                ByteBufCodecs.VAR_INT, Layout::offhand, ByteBufCodecs.VAR_INT, Layout::total, Layout::new);

        static Layout of(Inventory inventory) {
            return new Layout(inventory.items.size(), inventory.armor.size(), inventory.offhand.size(), inventory.getContainerSize());
        }
    }

    @Nullable private final ServerPlayer target;
    private final int storageStart, storageEnd, targetSlots;
    public final int height, viewerLabelY;

    public static int open(ServerPlayer viewer, ServerPlayer target) throws CommandSyntaxException {
        if (viewer == target) throw SELF.create();
        var layout = Layout.of(target.getInventory());
        viewer.openMenu(new SimpleMenuProvider((id, inventory, player) -> new AveInventoryMenu(id, inventory, target, layout),
                Component.translatable("container.chaosticspells.ave_inventory", target.getDisplayName())),
                buf -> Layout.STREAM_CODEC.encode(buf, layout));
        return 1;
    }

    public static AveInventoryMenu client(int id, Inventory viewer, RegistryFriendlyByteBuf buf) {
        return new AveInventoryMenu(id, viewer, null, Layout.STREAM_CODEC.decode(buf));
    }

    private AveInventoryMenu(int id, Inventory viewer, @Nullable ServerPlayer target, Layout layout) {
        super(ChaosticMenuRegistry.AVE_INVENTORY.get(), id);
        this.target = target;
        Container container = target != null ? target.getInventory() : new SimpleContainer(layout.total());
        // The server copies the target's own slot rules. The client only predicts with its player's matching slots.
        var rules = target != null ? target.getInventory() : viewer;
        int armorStart = layout.items(), offhandStart = armorStart + layout.armor(), extraStart = offhandStart + layout.offhand();
        int hotbar = Math.min(Inventory.getSelectionSize(), layout.items());

        var equipment = new ArrayList<Integer>();
        for (int i = layout.armor() - 1; i >= 0; i--) equipment.add(armorStart + i); // Head first, as on the player's screen.
        if (layout.armor() > 0 && layout.offhand() > 0) equipment.add(-1);
        for (int i = 0; i < layout.offhand(); i++) equipment.add(offhandStart + i);
        int y = section(container, rules, equipment, 18);
        storageStart = slots.size();
        y = section(container, rules, range(hotbar, layout.items()), y + 4);
        y = section(container, rules, range(0, hotbar), y + 4);
        storageEnd = slots.size();
        var extra = range(extraStart, layout.total());
        y = section(container, rules, extra, extra.isEmpty() ? y : y + 4);
        targetSlots = slots.size();

        viewerLabelY = y + 2;
        int top = y + 13;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(viewer, 9 + row * 9 + column, 8 + column * 18, top + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(viewer, column, 8 + column * 18, top + 58));
        height = top + 83;
    }

    private static List<Integer> range(int from, int to) {
        return IntStream.range(from, Math.max(from, to)).boxed().toList();
    }

    /** Lays out container indices nine per row (-1 leaves a gap) and returns the y below the last row. */
    private int section(Container container, Inventory rules, List<Integer> indices, int y) {
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            if (index >= 0) addSlot(new TargetSlot(container, index, 8 + i % 9 * 18, y + i / 9 * 18, rule(rules, index), target != null));
        }
        return y + (indices.size() + 8) / 9 * 18;
    }

    @Nullable
    private static Slot rule(Inventory inventory, int index) {
        for (var slot : inventory.player.inventoryMenu.slots) {
            if (slot.container == inventory && slot.getContainerSlot() == index) return slot;
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack = slot.getItem();
        var result = stack.copy();
        boolean moved = index < targetSlots
                ? moveItemStackTo(stack, targetSlots, slots.size(), true)
                : moveItemStackTo(stack, storageStart, storageEnd, false);
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY, result);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        // Logging out or respawning removes the target's ServerPlayer, which closes the view.
        return target == null || !target.isRemoved();
    }

    private static final class TargetSlot extends Slot {
        @Nullable private final Slot rule;
        private final boolean server;

        TargetSlot(Container container, int index, int x, int y, @Nullable Slot rule, boolean server) {
            super(container, index, x, y);
            this.rule = rule;
            this.server = server;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return rule == null || rule.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            // The client's rule slot holds the viewer's own item, so only the server can judge pickups.
            return !server || rule == null || rule.mayPickup(player);
        }

        @Override
        public int getMaxStackSize() {
            return rule == null ? super.getMaxStackSize() : rule.getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return rule == null ? super.getMaxStackSize(stack) : rule.getMaxStackSize(stack);
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack previous) {
            // On the server the rule slot views this same index, so its side effects (equip sounds) reach the target.
            if (server && rule != null) rule.setByPlayer(stack, previous);
            else super.setByPlayer(stack, previous);
        }

        @Nullable
        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return rule == null ? null : rule.getNoItemIcon();
        }
    }
}

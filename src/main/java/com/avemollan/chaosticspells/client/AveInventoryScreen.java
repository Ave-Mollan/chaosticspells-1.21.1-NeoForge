package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.commands.AveInventoryMenu;
import com.avemollan.chaosticspells.registry.ChaosticMenuRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Drawn without a texture because the slot count depends on the target's (possibly modded) inventory. */
@EventBusSubscriber(modid = ChaosticSpells.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class AveInventoryScreen extends AbstractContainerScreen<AveInventoryMenu> {
    public AveInventoryScreen(AveInventoryMenu menu, Inventory viewer, Component title) {
        super(menu, viewer, title);
        imageHeight = menu.height;
        inventoryLabelY = menu.viewerLabelY;
    }

    @SubscribeEvent
    static void register(RegisterMenuScreensEvent event) {
        event.register(ChaosticMenuRegistry.AVE_INVENTORY.get(), AveInventoryScreen::new);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos, w = imageWidth, h = imageHeight;
        // Vanilla container panel: black rounded outline, white/grey bevel, light grey body.
        graphics.fill(x + 1, y, x + w - 1, y + h, 0xFF000000);
        graphics.fill(x, y + 1, x + w, y + h - 1, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFFFFFFF);
        graphics.fill(x + 3, y + 3, x + w - 1, y + h - 1, 0xFF555555);
        graphics.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFFC6C6C6);
        for (var slot : menu.slots) {
            int sx = x + slot.x - 1, sy = y + slot.y - 1;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
            graphics.fill(sx + 1, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }
    }
}

package com.avemollan.chaosticspells.client;

import io.redspace.ironsspellbooks.render.ClientStaffItemExtensions;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BonescallerStaffClientExtensions extends ClientStaffItemExtensions {
    private BonescallerStaffItemRenderer renderer;

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (this.renderer == null) {
            this.renderer = new BonescallerStaffItemRenderer();
        }
        return this.renderer;
    }
}

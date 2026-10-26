package com.avemollan.chaosticspells.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class OwnersWhipClientExtensions implements IClientItemExtensions {
    private BlockEntityWithoutLevelRenderer renderer;
    @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new OwnersWhipRenderer();return renderer;}
}

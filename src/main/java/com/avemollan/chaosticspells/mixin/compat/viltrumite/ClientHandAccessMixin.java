package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;

/** The config plugin guards only Core's merged offhand callback, preserving vanilla hand rendering. */
@Mixin(value = ItemInHandRenderer.class, priority = 0)
public abstract class ClientHandAccessMixin {}

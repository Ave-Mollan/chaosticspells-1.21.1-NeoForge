package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeTabs.class)
public interface CreativeTabsAccess {
    @Accessor("CACHED_PARAMETERS")
    static void chaosticspells$invalidate(CreativeModeTab.ItemDisplayParameters parameters) {
        throw new AssertionError("Mixin accessor was not applied");
    }
}

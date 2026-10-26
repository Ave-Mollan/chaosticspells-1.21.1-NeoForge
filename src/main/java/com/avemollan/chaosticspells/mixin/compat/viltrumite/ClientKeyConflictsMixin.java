package com.avemollan.chaosticspells.mixin.compat.viltrumite;

import com.avemollan.chaosticspells.compat.viltrumite.ViltrumiteClientPolicy;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class ClientKeyConflictsMixin {
    @ModifyExpressionValue(method = "refreshEntry", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"))
    private KeyMapping[] ave$hideInactiveConflicts(KeyMapping[] keys) {
        return ViltrumiteClientPolicy.visibleKeys(keys);
    }
}

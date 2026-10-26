package com.avemollan.chaosticspells.mixin;

import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.mojang.brigadier.StringReader;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The transmitted tree uses vanilla argument types; allow their client parser only inside the owner's aliases. */
@Mixin(EntityArgument.class)
public abstract class ClientAveEntityArgumentMixin {
    @Inject(method = "parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lnet/minecraft/commands/arguments/selector/EntitySelector;",
            at = @At("HEAD"), cancellable = true)
    private void chaosticspells$aveSelectors(StringReader reader, Object source, CallbackInfoReturnable<EntitySelector> result)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = Minecraft.getInstance().player;
        if (!(source instanceof ClientSuggestionProvider) || player == null || !OwnersWhipItem.isOwner(player)) return;
        String prefix = reader.getString().substring(0, reader.getCursor());
        if (prefix.startsWith("/")) prefix = prefix.substring(1);
        for (String command : List.of("give", "effect", "damage", "kill", "locate", "tp")) {
            if (prefix.startsWith("ave " + command + " ")) {
                result.setReturnValue(((EntityArgument) (Object) this).parse(reader));
                return;
            }
        }
    }
}

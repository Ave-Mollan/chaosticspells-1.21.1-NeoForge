package com.avemollan.chaosticspells.commands;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Owner-only copies of vanilla command trees plus /ave inventory. The original trees and the player's OP status stay intact. */
@EventBusSubscriber(modid = ChaosticSpells.MODID)
public final class AveCommands {
    private static final SimpleCommandExceptionType DENIED = new SimpleCommandExceptionType(
            Component.translatable("commands.generic.permission"));

    public static boolean isOwner(CommandSourceStack source) {
        // withEntity (including /execute as) keeps the original sender in source.source.
        return source.source instanceof ServerPlayer sender && sender == source.getEntity()
                && OwnersWhipItem.isOwner(sender);
    }

    private static CommandSourceStack privileged(CommandSourceStack source) throws CommandSyntaxException {
        if (!isOwner(source)) throw DENIED.create();
        return source.withPermission(Commands.LEVEL_GAMEMASTERS);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("ave").requires(AveCommands::isOwner);
        for (String name : List.of("give", "effect", "damage", "kill", "locate", "tp", "gamemode", "summon", "attribute")) {
            // Copy the canonical teleport tree so non-OP clients don't need the hidden /teleport root.
            var original = event.getDispatcher().getRoot().getChild(name.equals("tp") ? "teleport" : name);
            if (original != null) root.then(copy(original, name, new IdentityHashMap<>()));
        }
        var inventory = Commands.literal("inventory")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> AveInventoryMenu.open(context.getSource().getPlayerOrException(),
                                EntityArgument.getPlayer(context, "target"))))
                .build();
        root.then(copy(inventory, "inventory", new IdentityHashMap<>()));
        // Build before registering: register() returns a detached node when /ave already exists (e.g. /ave race).
        event.getDispatcher().register(root);
    }

    private static CommandNode<CommandSourceStack> copy(CommandNode<CommandSourceStack> original, String name,
            Map<CommandNode<CommandSourceStack>, CommandNode<CommandSourceStack>> copies) {
        var existing = copies.get(original);
        if (existing != null) return existing;
        ArgumentBuilder<CommandSourceStack, ?> builder = name == null ? original.createBuilder() : Commands.literal(name);
        builder.requires(source -> isOwner(source) && original.canUse(source.withPermission(Commands.LEVEL_GAMEMASTERS)));
        if (original.getCommand() != null) {
            builder.executes(context -> original.getCommand().run(context.copyFor(privileged(context.getSource()))));
        }
        if (builder instanceof RequiredArgumentBuilder<?, ?>) {
            @SuppressWarnings("unchecked") // createBuilder preserves the original node's source type.
            var argument = (RequiredArgumentBuilder<CommandSourceStack, ?>) builder;
            argument.suggests((context, suggestions) -> isOwner(context.getSource())
                    ? original.listSuggestions(context.copyFor(privileged(context.getSource())), suggestions)
                    : Suggestions.empty());
        }
        var result = builder.build();
        if (result instanceof ArgumentCommandNode<?, ?>) {
            @SuppressWarnings("unchecked")
            var argument = (ArgumentCommandNode<CommandSourceStack, ?>) result;
            result = new ScopedArgument<>(argument);
        }
        copies.put(original, result);
        for (var child : original.getChildren()) {
            // The copied trees have no redirects. A redirect added by another mod could lead outside them
            // (e.g. to the root, like /execute run) with the elevated source, so such branches are not copied.
            if (child.getRedirect() == null) result.addChild(copy(child, null, copies));
        }
        return result;
    }

    /** Vanilla selector arguments check permissions during parsing as well as execution. */
    private static final class ScopedArgument<T> extends ArgumentCommandNode<CommandSourceStack, T> {
        ScopedArgument(ArgumentCommandNode<CommandSourceStack, T> node) {
            super(node.getName(), node.getType(), node.getCommand(), node.getRequirement(), node.getRedirect(),
                    node.getRedirectModifier(), node.isFork(), node.getCustomSuggestions());
        }

        @Override
        public void parse(StringReader reader, CommandContextBuilder<CommandSourceStack> context) throws CommandSyntaxException {
            var source = context.getSource();
            context.withSource(privileged(source));
            try {
                super.parse(reader, context);
            } finally {
                // Never leave the dispatcher or subsequent commands with an elevated source.
                context.withSource(source);
            }
        }
    }

    private AveCommands() {}
}

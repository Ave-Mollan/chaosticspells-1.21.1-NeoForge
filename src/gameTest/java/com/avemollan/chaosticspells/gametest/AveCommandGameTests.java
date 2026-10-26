package com.avemollan.chaosticspells.gametest;

import com.avemollan.chaosticspells.commands.AveCommands;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.server.command.CommandHelper;

@GameTestHolder("chaosticspells_tests")
@PrefixGameTestTemplate(false)
public final class AveCommandGameTests {
    private static final List<String> ALIASES = List.of("give", "effect", "damage", "kill", "locate", "tp");

    @GameTest(template = "empty", batch = "ave_commands_tree")
    public static void onlyTheOwnerReceivesThePrivilegedCommandTree(GameTestHelper h) {
        var owner = player(h, OwnersWhipItem.OWNER_UUID).createCommandSourceStack().withPermission(0);
        var admin = player(h, UUID.randomUUID()).createCommandSourceStack().withPermission(4);
        var root = commands(h).getRoot();
        h.assertTrue(root.getChild("ave").canUse(owner), "Owner must have /ave without OP, with or without optional mods");
        h.assertTrue(!root.getChild("ave").canUse(admin), "Another operator with the same name must not have /ave");
        for (String name : ALIASES) {
            var original = root.getChild(name);
            h.assertTrue(!original.canUse(owner) && original.canUse(admin), "Native /" + name + " permissions must remain unchanged");
        }
        var ownTree = visibleTree(h, owner);
        h.assertTrue(ownTree.getChild("give") == null, "Owner must not receive unprefixed operator commands");
        for (String name : ALIASES) h.assertTrue(ownTree.getChild("ave").getChild(name) != null, "Missing visible alias: " + name);
        h.assertTrue(ownTree.getChild("ave").getChild("tp").getChild("location") != null,
                "/ave tp must expose its real arguments to non-OP clients without the hidden /teleport root");
        for (String name : List.of("op", "execute", "gamemode", "stop")) {
            h.assertTrue(ownTree.getChild("ave").getChild(name) == null, "Only the six requested commands may be elevated");
        }
        h.assertTrue(visibleTree(h, admin).getChild("ave") == null, "The tree sent to other operators must omit /ave entirely");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "ave_commands_access")
    public static void operatorsConsoleAndExecuteAsCannotImpersonateTheOwner(GameTestHelper h) throws Exception {
        var owner = player(h, OwnersWhipItem.OWNER_UUID);
        var admin = player(h, UUID.randomUUID()).createCommandSourceStack().withPermission(4);
        var console = h.getLevel().getServer().createCommandSourceStack();
        for (var forbidden : List.of(admin, admin.withEntity(owner), console, console.withEntity(owner))) {
            h.assertTrue(!AveCommands.isOwner(forbidden), "An impersonated entity UUID must not replace the real command sender");
            for (String name : ALIASES) {
                boolean denied = false;
                try { commands(h).execute("ave " + name, forbidden); }
                catch (CommandSyntaxException expected) { denied = true; }
                h.assertTrue(denied, "Non-owner unexpectedly executed /ave " + name);
            }
        }
        // Also check the execution callback itself instead of relying solely on tree visibility.
        String input = "ave give @s minecraft:diamond";
        var context = commands(h).parse(input, owner.createCommandSourceStack().withPermission(0)).getContext().build(input);
        boolean denied = false;
        try { context.getCommand().run(context.copyFor(admin)); }
        catch (CommandSyntaxException expected) { denied = true; }
        h.assertTrue(denied, "Execution must recheck the sender after parsing");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "ave_commands_execute")
    public static void executeAsAnotherEntityCannotBorrowTheOwnersAccess(GameTestHelper h) {
        var owner = player(h, OwnersWhipItem.OWNER_UUID);
        var target = h.spawn(EntityType.PIG, new BlockPos(2, 1, 2));
        target.addTag("ave_execute_test");
        var commands = h.getLevel().getServer().getCommands();
        String selector = "@e[tag=ave_execute_test,limit=1]";
        // Real dispatch path: brigadier parses /ave with the outer source, the execution recheck must reject the pig.
        commands.performPrefixedCommand(owner.createCommandSourceStack().withPermission(4),
                "execute as " + selector + " run ave kill @s");
        h.assertTrue(target.isAlive(), "/execute as <other entity> run ave must not run with the owner's access");
        commands.performPrefixedCommand(h.getLevel().getServer().createCommandSourceStack(),
                "execute as " + selector + " run ave kill @s");
        h.assertTrue(target.isAlive(), "Console /execute must not reach /ave");
        commands.performPrefixedCommand(owner.createCommandSourceStack().withPermission(4),
                "execute at " + selector + " run ave kill " + selector);
        h.assertTrue(!target.isAlive(), "Execute contexts that keep the owner as the entity must still work");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "ave_commands_inventory")
    public static void giveAndEffectAcceptSelectorsWithoutGrantingOp(GameTestHelper h) throws Exception {
        var owner = player(h, OwnersWhipItem.OWNER_UUID);
        var source = owner.createCommandSourceStack().withPermission(0);
        commands(h).execute("ave give @s minecraft:diamond 3", source);
        h.assertTrue(owner.getInventory().countItem(Items.DIAMOND) == 3, "Give must deliver the requested item count");
        commands(h).execute("ave effect give @s minecraft:speed 10 1 true", source);
        var effect = owner.getEffect(MobEffects.MOVEMENT_SPEED);
        h.assertTrue(effect != null && effect.getAmplifier() == 1 && effect.getDuration() == 200, "Effect arguments must retain vanilla meaning");
        commands(h).execute("ave effect clear @s minecraft:speed", source);
        h.assertTrue(!owner.hasEffect(MobEffects.MOVEMENT_SPEED), "Effect clear must work too");
        h.assertTrue(!source.hasPermission(2) && !h.getLevel().getServer().getPlayerList().isOp(owner.getGameProfile()),
                "Running an alias must not grant persistent permissions or OP");
        var invalid = commands(h).parse("ave give @s nonexistent:item", source);
        h.assertTrue(!invalid.getContext().getSource().hasPermission(2), "Even failed argument parsing must restore original permissions");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "ave_commands_damage")
    public static void damageAndKillRunVanillaActionsWithoutOp(GameTestHelper h) throws Exception {
        var owner = player(h, OwnersWhipItem.OWNER_UUID);
        var source = owner.createCommandSourceStack().withPermission(0);
        var target = h.spawn(EntityType.PIG, new BlockPos(2, 1, 2));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);
        target.setHealth(40); target.addTag("ave_command_test");
        commands(h).execute("ave damage @e[tag=ave_command_test,limit=1] 4 minecraft:generic", source);
        h.assertTrue(Math.abs(target.getHealth() - 36) < .001, "Damage must resolve privileged entity selectors and damage types");
        commands(h).execute("ave kill " + target.getUUID(), source);
        h.assertTrue(!target.isAlive(), "Kill must affect the requested target");
        commands(h).execute("ave kill", source);
        h.assertTrue(!owner.isAlive(), "Kill without targets must retain vanilla self-targeting");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "ave_commands_tp")
    public static void teleportPreservesThePlayersRelativeCoordinates(GameTestHelper h) throws Exception {
        var owner = player(h, OwnersWhipItem.OWNER_UUID);
        // FakePlayer's listener intentionally ignores teleport. Use the real listener's movement
        // implementation, stubbing only network output, so this verifies actual vanilla teleport.
        var connection = new Connection(PacketFlow.SERVERBOUND) {
            @Override public void setListenerForServerboundHandshake(PacketListener listener) {}
        };
        owner.connection = new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, owner,
                CommonListenerCookie.createInitial(owner.getGameProfile(), false)) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet, PacketSendListener listener) {}
        };
        double x = owner.getX(), y = owner.getY(), z = owner.getZ();
        commands(h).execute("ave tp ~1 ~2 ~3", owner.createCommandSourceStack().withPermission(0));
        h.assertTrue(Math.abs(owner.getX() - x - 1) < .001 && Math.abs(owner.getY() - y - 2) < .001
                && Math.abs(owner.getZ() - z - 3) < .001, "Teleport must use the owner position rather than console coordinates");
        commands(h).execute("ave tp @s ~1 ~ ~-1 90 0", owner.createCommandSourceStack().withPermission(0));
        h.assertTrue(Math.abs(owner.getX() - x - 2) < .001 && Math.abs(owner.getZ() - z - 2) < .001
                && Math.abs(owner.getYRot() - 90) < .001, "Explicit selector and rotation syntax must also work");
        h.succeed();
    }

    @GameTest(template = "empty", batch = "ave_commands_locate")
    public static void locateAndNativeArgumentSuggestionsWorkWithoutOp(GameTestHelper h) throws Exception {
        var source = player(h, OwnersWhipItem.OWNER_UUID).createCommandSourceStack().withPermission(0);
        String biome = h.getLevel().getBiome(BlockPos.containing(source.getPosition())).unwrapKey().orElseThrow().location().toString();
        commands(h).execute("ave locate biome " + biome, source);
        var structure = commands(h).parse("ave locate structure minecraft:village_plains", source);
        h.assertTrue(structure.getExceptions().isEmpty() && !structure.getReader().canRead(), "Structure locate syntax must remain available");
        for (var query : List.of(new String[]{"ave give @", "@s"}, new String[]{"ave give @s minecraft:diam", "minecraft:diamond"},
                new String[]{"ave effect give @s minecraft:spe", "minecraft:speed"})) {
            var suggestions = commands(h).getCompletionSuggestions(commands(h).parse(query[0], source)).join();
            h.assertTrue(suggestions.getList().stream().anyMatch(s -> s.getText().equals(query[1])), "Missing native suggestion: " + query[1]);
        }
        h.succeed();
    }

    private static RootCommandNode<CommandSourceStack> visibleTree(GameTestHelper h, CommandSourceStack source) {
        var result = new RootCommandNode<CommandSourceStack>();
        // This is the filtering/copy routine used by Minecraft's Commands.sendCommands on NeoForge.
        CommandHelper.mergeCommandNode(commands(h).getRoot(), result, new IdentityHashMap<>(), source, context -> 0, suggestion -> suggestion);
        return result;
    }

    private static CommandDispatcher<CommandSourceStack> commands(GameTestHelper h) {
        return h.getLevel().getServer().getCommands().getDispatcher();
    }

    private static ServerPlayer player(GameTestHelper h, UUID uuid) {
        var profile = new GameProfile(uuid, "AveMollan");
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), profile, ClientInformation.createDefault());
        player.connection = FakePlayerFactory.get(h.getLevel(), profile).connection;
        player.moveTo(h.absolutePos(new BlockPos(1, 1, 1)).getBottomCenter());
        return player;
    }
}

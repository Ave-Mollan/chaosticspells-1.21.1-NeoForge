package com.avemollan.chaosticspells.gametest;

import java.util.ArrayList;
import java.util.UUID;
import com.avemollan.chaosticspells.events.WhipCombat;
import com.avemollan.chaosticspells.events.WhipRetention;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.network.WhipActionPayload;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("chaosticspells_tests")
@PrefixGameTestTemplate(false)
public final class WhipGameTests {
    /** Any UUID: ownership is decided by the player name alone. */
    private static final UUID OWNER = UUID.randomUUID();
    // Separate batches prevent concurrent tests from sharing the hardcoded owner's charge state.
    @GameTest(template = "empty", batch = "whip_1", timeoutTicks = 70)
    public static void quickLashBypassesArmorAndKnocksBack(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        Zombie target = target(helper);
        target.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        target.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        target.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        target.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        WhipCombat.handle(owner, WhipActionPayload.START);
        WhipCombat.handle(owner, WhipActionPayload.RELEASE);
        helper.assertTrue(target.getHealth() == 100, "Damage must wait for the audible crack, not happen on button release");
        advance(helper, owner, 1, 51);
        helper.runAfterDelay(52, () -> {
            helper.assertTrue(Math.abs(target.getHealth() - 95) < .001, "A single sweep must deal exactly 5 through netherite; health=" + target.getHealth());
            helper.assertTrue(target.getDeltaMovement().z > .2, "The hit must impart outward motion");
            target.invulnerableTime = 0;
            WhipCombat.handle(owner, WhipActionPayload.RELEASE);
            helper.assertTrue(target.getHealth() == 95, "Repeated release packets must not inflict additional damage");
            owner.getCooldowns().removeCooldown(ChaosticItemRegistry.OWNERS_WHIP.get());
            WhipCombat.handle(owner, WhipActionPayload.START);
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(owner, owner.damageSources().generic()));
            WhipCombat.handle(owner, WhipActionPayload.RELEASE);
            helper.assertTrue(target.getHealth() == 95, "A charge must not survive death");
            finish(helper, owner, target);
        });
    }

    @GameTest(template = "empty", batch = "whip_2", timeoutTicks = 130)
    public static void serverTimedChargeReachesTwentyDamage(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        Zombie target = target(helper);
        WhipCombat.handle(owner, WhipActionPayload.START);
        helper.runAfterDelay(45, () -> {
            WhipCombat.handle(owner, WhipActionPayload.RELEASE);
            helper.assertTrue(target.getHealth() == 100, "Charged damage must wait for the recording's first crack");
        });
        advance(helper, owner, 46, 97);
        helper.runAfterDelay(71, () -> {
            // Past the follow-up window, while the stroke still runs and the server cooldown holds.
            WhipCombat.handle(owner, WhipActionPayload.START);
            WhipCombat.handle(owner, WhipActionPayload.RELEASE);
        });
        helper.runAfterDelay(80, () -> helper.assertTrue(target.getHealth() == 80, "A full-charge single lash must deal 20; health="+target.getHealth()));
        helper.runAfterDelay(96, () -> {
            helper.assertTrue(target.getHealth() == 80, "Full charge, the active sequence and the cooldown must not create a second stroke; health="+target.getHealth());
            helper.assertTrue(OwnersWhipItem.damageForTicks(10000) == 20, "Damage remains capped at 20");
        });
        helper.runAfterDelay(98, () -> finish(helper, owner, target));
    }

    @GameTest(template = "empty", batch = "whip_3")
    public static void differentUuidAndVanillaAttacksAreBlocked(GameTestHelper helper) {
        // Matching the visible name must not grant access to a different UUID.
        ServerPlayer stranger = player(helper, UUID.randomUUID());
        Zombie target = target(helper);
        WhipCombat.handle(stranger, WhipActionPayload.START);
        WhipCombat.handle(stranger, WhipActionPayload.RELEASE);
        helper.assertTrue(target.getHealth() == 100, "Another UUID must not use the whip");
        var attack = new AttackEntityEvent(stranger, target);
        NeoForge.EVENT_BUS.post(attack);
        helper.assertTrue(attack.isCanceled(), "The normal melee path must also be blocked");
        finish(helper, stranger, target);
    }

    @GameTest(template = "empty", batch = "whip_4")
    public static void wallsRangeAndSlotChangesPreventHits(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        Zombie target = target(helper);
        helper.setBlock(new BlockPos(1, 1, 3), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 2, 3), Blocks.STONE);
        helper.assertTrue(WhipCombat.findTarget(owner) == null, "A solid wall must block the targeting ray");
        helper.setBlock(new BlockPos(1, 1, 3), Blocks.AIR);
        helper.setBlock(new BlockPos(1, 2, 3), Blocks.AIR);
        helper.assertTrue(WhipCombat.findTarget(owner) == target, "An unobstructed target in range must be found");
        double z = target.getZ();
        target.setPos(target.getX(), target.getY(), owner.getZ() + 10);
        helper.assertTrue(WhipCombat.findTarget(owner) == null, "Targets beyond eight blocks must not be hit");
        target.setPos(target.getX(), target.getY(), z);
        WhipCombat.handle(owner, WhipActionPayload.START);
        owner.getInventory().selected = 1;
        owner.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ChaosticItemRegistry.OWNERS_WHIP.get()));
        WhipCombat.handle(owner, WhipActionPayload.RELEASE);
        helper.assertTrue(target.getHealth() == 100, "Switching to another whip must discard the old charge");
        finish(helper, owner, target);
    }

    @GameTest(template = "empty", batch = "whip_5")
    public static void deathDropsReturnExactStackOnce(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        ItemStack whip = owner.getMainHandItem().copy();
        whip.set(DataComponents.CUSTOM_NAME, Component.literal("Persistent test whip"));
        owner.getInventory().clearContent();
        var drops = new ArrayList<ItemEntity>();
        drops.add(new ItemEntity(helper.getLevel(), owner.getX(), owner.getY(), owner.getZ(), whip));
        drops.add(new ItemEntity(helper.getLevel(), owner.getX(), owner.getY(), owner.getZ(), new ItemStack(Items.DIAMOND)));
        NeoForge.EVENT_BUS.post(new LivingDropsEvent(owner, owner.damageSources().generic(), drops, false));
        helper.assertTrue(drops.size() == 1 && drops.getFirst().getItem().is(Items.DIAMOND), "Only the whip must leave the death drops");
        ServerPlayer respawned = player(helper, OWNER);
        respawned.getInventory().clearContent();
        NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(respawned, owner, true));
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(respawned, false));
        helper.assertTrue(respawned.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 1, "The respawned owner must regain exactly one whip");
        helper.assertTrue(respawned.getInventory().items.stream().anyMatch(stack -> stack.getHoverName().getString().equals("Persistent test whip")),
                "Custom item components must survive death");
        WhipRetention.restore(respawned);
        helper.assertTrue(respawned.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 1, "Retrying restoration must not duplicate the whip");
        WhipCombat.cancel(owner);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "whip_6")
    public static void fullInventoryDefersRestorationWithoutLoss(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        var drops = new ArrayList<ItemEntity>();
        drops.add(new ItemEntity(helper.getLevel(), owner.getX(), owner.getY(), owner.getZ(), owner.getMainHandItem().copy()));
        NeoForge.EVENT_BUS.post(new LivingDropsEvent(owner, owner.damageSources().generic(), drops, false));
        for (int i = 0; i < owner.getInventory().items.size(); i++) owner.getInventory().items.set(i, new ItemStack(Items.COBBLESTONE, 64));
        WhipRetention.restore(owner);
        helper.assertTrue(owner.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 0, "A full inventory must keep the whip pending");
        // Serialize and reload pending data to exercise the disconnect/save path as well.
        var saved = owner.getPersistentData().copy();
        owner.getPersistentData().merge(saved);
        owner.getInventory().items.set(5, ItemStack.EMPTY);
        WhipRetention.restore(owner);
        helper.assertTrue(owner.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 1 && drops.isEmpty(),
                "The pending whip must be restored when space opens, with no world drop");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "whip_7")
    public static void nonOwnerDropsAndEmptyRespawnDoNotCreateWhips(GameTestHelper helper) {
        ServerPlayer stranger = player(helper, UUID.randomUUID());
        var drops = new ArrayList<ItemEntity>();
        drops.add(new ItemEntity(helper.getLevel(), stranger.getX(), stranger.getY(), stranger.getZ(), stranger.getMainHandItem().copy()));
        NeoForge.EVENT_BUS.post(new LivingDropsEvent(stranger, stranger.damageSources().generic(), drops, false));
        helper.assertTrue(drops.size() == 1, "A non-owner's whip must remain a normal drop");
        ServerPlayer owner = player(helper, OWNER);
        owner.getInventory().clearContent();
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(owner, false));
        helper.assertTrue(owner.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 0,
                "Respawning without a saved whip must never generate one");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "whip_8", timeoutTicks = 160)
    public static void hitActuallyMovesTargetSeveralBlocks(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        var forcedChunks = new java.util.HashSet<net.minecraft.world.level.ChunkPos>();
        for (int z = 0; z < 14; z++) {
            for (int x = 0; x < 3; x++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                for (int y = 1; y < 4; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                forcedChunks.add(new net.minecraft.world.level.ChunkPos(helper.absolutePos(new BlockPos(x, 0, z))));
            }
        }
        Zombie target = target(helper);
        target.setNoGravity(false);
        target.setNoAi(false);
        target.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        // Chunk activation is asynchronous on a server without connected players. Wait for real
        // entity ticks, and keep the whole flight lane active even when it crosses a chunk border.
        for (var chunk : forcedChunks) helper.getLevel().setChunkForced(chunk.x, chunk.z, true);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(target.tickCount >= 5, "Waiting for the target's chunk to start ticking"))
                .thenExecute(() -> {
                    double initialZ = target.getZ();
                    int initialTicks = target.tickCount;
                    WhipCombat.handle(owner, WhipActionPayload.START);
                    WhipCombat.handle(owner, WhipActionPayload.RELEASE);
                    advance(helper, owner, 1, 64);
                    helper.runAfterDelay(65, () -> {
                        helper.assertTrue(target.getHealth() == 95, "The physical sweep must have dealt 5 damage");
                        double distance = target.getZ() - initialZ;
                        for (var chunk : forcedChunks) helper.getLevel().setChunkForced(chunk.x, chunk.z, false);
                        helper.assertTrue(distance >= 3 && distance <= 9, "A normal hit must move the target several blocks on a clear floor; distance=" + distance
                                + ", entityTicks=" + (target.tickCount-initialTicks) + ", velocity=" + target.getDeltaMovement());
                        finish(helper, owner, target);
                    });
                });
    }

    @GameTest(template = "empty", batch = "whip_9", timeoutTicks = 70)
    public static void sweepHitsMultipleFrontTargetsButNotBehindOrBeyondReach(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        Zombie center = target(helper), left = target(helper), right = target(helper), behind = target(helper), far = target(helper);
        left.setPos(owner.getX()-3, center.getY(), owner.getZ()+4);
        right.setPos(owner.getX()+3, center.getY(), owner.getZ()+4);
        behind.setPos(owner.getX(), center.getY(), owner.getZ()-3);
        far.setPos(owner.getX(), center.getY(), owner.getZ()+10);
        // Clear the small template barriers along both lateral sight lines.
        for (int x=-3;x<=5;x++) for(int z=0;z<=6;z++) for(int y=1;y<=3;y++) helper.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        WhipCombat.handle(owner,WhipActionPayload.START);
        WhipCombat.handle(owner,WhipActionPayload.RELEASE);
        advance(helper,owner,1,34);
        helper.runAfterDelay(35, () -> {
            helper.assertTrue(center.getHealth()==95 && left.getHealth()==95 && right.getHealth()==95,
                    "All three targets in the frontal arc must be hit once; health="+center.getHealth()+","+left.getHealth()+","+right.getHealth());
            helper.assertTrue(behind.getHealth()==100 && far.getHealth()==100,"The swing must not hit behind the owner or outside eight blocks");
            for (Zombie zombie : new Zombie[]{left,right,behind,far}) zombie.discard();
            finish(helper,owner,center);
        });
    }

    @GameTest(template = "empty", batch = "whip_10", timeoutTicks = 70)
    public static void areaSweepDoesNotDamageThroughWalls(GameTestHelper helper) {
        ServerPlayer owner=player(helper,OWNER);
        Zombie blocked=target(helper);
        helper.setBlock(new BlockPos(1,1,3),Blocks.STONE);
        helper.setBlock(new BlockPos(1,2,3),Blocks.STONE);
        WhipCombat.handle(owner,WhipActionPayload.START);
        WhipCombat.handle(owner,WhipActionPayload.RELEASE);
        advance(helper,owner,1,34);
        helper.runAfterDelay(35, () -> {
            helper.assertTrue(blocked.getHealth()==100,"The broad attack volume must still stop at solid blocks");
            finish(helper,owner,blocked);
        });
    }

    @GameTest(template = "empty", batch = "whip_11", timeoutTicks = 100)
    public static void repeatPressQueuesExactlyOneFollowUp(GameTestHelper helper) {
        ServerPlayer owner=player(helper,OWNER);
        Zombie target=target(helper);
        WhipCombat.handle(owner,WhipActionPayload.START);
        WhipCombat.handle(owner,WhipActionPayload.RELEASE);
        helper.runAfterDelay(8, () -> {
            WhipCombat.handle(owner,WhipActionPayload.START);
            WhipCombat.handle(owner,WhipActionPayload.RELEASE);
        });
        helper.runAfterDelay(9, () -> WhipCombat.handle(owner,WhipActionPayload.START));
        advance(helper,owner,1,74);
        helper.runAfterDelay(29, () -> helper.assertTrue(target.getHealth()==95,"The first stroke must still hit only once"));
        helper.runAfterDelay(52, () -> helper.assertTrue(target.getHealth()==90,"A repeat press must add one five-damage follow-up"));
        helper.runAfterDelay(75, () -> {
            helper.assertTrue(target.getHealth()==90,"Repeated presses cannot add a third stroke");
            finish(helper,owner,target);
        });
    }

    @GameTest(template = "empty", batch = "whip_12", timeoutTicks = 90)
    public static void slotChangeCancelsQueuedFollowUp(GameTestHelper helper) {
        ServerPlayer owner=player(helper,OWNER);
        Zombie target=target(helper);
        WhipCombat.handle(owner,WhipActionPayload.START);
        WhipCombat.handle(owner,WhipActionPayload.RELEASE);
        helper.runAfterDelay(8, () -> WhipCombat.handle(owner,WhipActionPayload.START));
        helper.runAfterDelay(25, () -> {
            owner.getInventory().selected=1;
            owner.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ChaosticItemRegistry.OWNERS_WHIP.get()));
        });
        advance(helper,owner,1,70);
        helper.runAfterDelay(71, () -> {
            helper.assertTrue(target.getHealth()==95,"Switching slots must cancel a queued second stroke");
            finish(helper,owner,target);
        });
    }

    @GameTest(template = "empty", batch = "whip_13")
    public static void deathRetainsCursorWhipAndSurvivesNonDeathClone(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        owner.containerMenu.setCarried(new ItemStack(ChaosticItemRegistry.OWNERS_WHIP.get()));
        NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(owner, owner.damageSources().generic()));
        helper.assertTrue(owner.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 0 && owner.containerMenu.getCarried().isEmpty(),
                "Death must take the held and cursor whips out of reach of grave mods and menu-close drops");
        // The End exit portal recreates the player through a clone with wasDeath=false.
        ServerPlayer returned = player(helper, OWNER);
        returned.getInventory().clearContent();
        NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(returned, owner, false));
        WhipRetention.restore(returned);
        helper.assertTrue(returned.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 2, "Both retained whips must follow every clone");
        WhipCombat.cancel(owner);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "whip_14")
    public static void creativeFullInventoryKeepsWhipPending(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        var drops = new ArrayList<ItemEntity>();
        drops.add(new ItemEntity(helper.getLevel(), owner.getX(), owner.getY(), owner.getZ(), owner.getMainHandItem().copy()));
        NeoForge.EVENT_BUS.post(new LivingDropsEvent(owner, owner.damageSources().generic(), drops, false));
        owner.getAbilities().instabuild = true;
        for (int i = 0; i < owner.getInventory().items.size(); i++) owner.getInventory().items.set(i, new ItemStack(Items.COBBLESTONE, 64));
        WhipRetention.restore(owner);
        owner.getInventory().items.set(5, ItemStack.EMPTY);
        WhipRetention.restore(owner);
        helper.assertTrue(owner.getInventory().countItem(ChaosticItemRegistry.OWNERS_WHIP.get()) == 1,
                "Inventory.add must not void the pending whip for a creative owner with a full inventory");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "whip_15")
    public static void sweepSparesOwnPetsArmorStandsAndMount(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OWNER);
        for (int x = -1; x <= 3; x++) for (int z = 0; z <= 6; z++) for (int y = 1; y <= 3; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
        Zombie control = target(helper);
        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(0, 1, 5));
        wolf.setNoAi(true);
        wolf.setOwnerUUID(owner.getUUID());
        wolf.setTame(true, false);
        var stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(2, 1, 5));
        var targets = WhipCombat.findTargets(owner);
        helper.assertTrue(targets.contains(control), "The control target in the arc must be found");
        helper.assertTrue(!targets.contains(wolf) && !targets.contains(stand), "Own pets and armor stands are not whip targets; targets=" + targets);
        wolf.discard();
        stand.discard();
        finish(helper, owner, control);
    }

    private static void advance(GameTestHelper helper,ServerPlayer owner,int first,int last) {
        // Mock players have no real client/entity tick; drive the production event at actual server tick times.
        for(int tick=first;tick<=last;tick++) helper.runAfterDelay(tick, () -> {
            owner.getCooldowns().tick();
            WhipCombat.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
        });
    }

    private static ServerPlayer player(GameTestHelper helper, UUID uuid) {
        var profile = new GameProfile(uuid, OWNER.equals(uuid) ? OwnersWhipItem.OWNER_NAME : "Stranger");
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile, ClientInformation.createDefault());
        player.connection = FakePlayerFactory.get(helper.getLevel(), profile).connection;
        player.moveTo(helper.absolutePos(new BlockPos(1, 1, 1)).getBottomCenter());
        player.setYRot(0);
        player.setXRot(0);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ChaosticItemRegistry.OWNERS_WHIP.get()));
        return player;
    }

    private static Zombie target(GameTestHelper helper) {
        // The tiny shared template has a barrier wall at z=3; open a firing lane for the long whip.
        helper.setBlock(new BlockPos(1, 1, 3), Blocks.AIR);
        helper.setBlock(new BlockPos(1, 2, 3), Blocks.AIR);
        Zombie zombie = helper.spawn(EntityType.HUSK, new BlockPos(1, 1, 4));
        zombie.setBaby(false);
        zombie.setNoAi(true);
        zombie.setNoGravity(true);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        zombie.setHealth(100);
        helper.getLevel().setChunkForced(zombie.chunkPosition().x, zombie.chunkPosition().z, true);
        return zombie;
    }

    private static String diagnostic(ServerPlayer player, Zombie target) {
        var start = player.getEyePosition();
        var block = player.level().clip(new net.minecraft.world.level.ClipContext(start,
                start.add(player.getLookAngle().scale(8)), net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, player));
        return "eye=" + start + ", target=" + target.getBoundingBox() + ", block=" + block.getBlockPos()
                + "/" + block.getType() + "/" + player.level().getBlockState(block.getBlockPos())
                + ", entities=" + player.level().getEntities(player, player.getBoundingBox().inflate(9)).size();
    }

    private static void finish(GameTestHelper helper, ServerPlayer player, Zombie target) {
        WhipCombat.cancel(player);
        helper.getLevel().setChunkForced(target.chunkPosition().x, target.chunkPosition().z, false);
        target.discard();
        helper.succeed();
    }
}

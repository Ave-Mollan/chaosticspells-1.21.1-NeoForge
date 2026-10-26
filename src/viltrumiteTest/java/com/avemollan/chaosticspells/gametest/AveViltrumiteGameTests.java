package com.avemollan.chaosticspells.gametest;

import com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess;
import com.avemollan.chaosticspells.compat.viltrumite.ViltrumiteIntegration;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.mojang.authlib.GameProfile;
import dev.baranhan.viltrumitecore.config.ViltrumiteCoreConfig;
import dev.baranhan.viltrumitecore.effect.ViltrumiteEffects;
import dev.baranhan.viltrumitecore.entity.MeteorEntity;
import dev.baranhan.viltrumitecore.event.CoreEvents;
import dev.baranhan.viltrumitecore.event.MeteorSpawnEvent;
import dev.baranhan.viltrumitecore.network.packet.RaceChoiceC2SPacket;
import dev.baranhan.viltrumitecore.network.packet.DashToggleC2SPacket;
import dev.baranhan.viltrumitecore.util.ViltrumiteCorePlayer;
import dev.baranhan.viltrumitecore.worldevent.WorldEventManager;
import dev.baranhan.viltrumiteflight.network.packet.FlightToggleC2SPacket;
import dev.baranhan.viltrumiteflight.util.FlightState;
import dev.baranhan.viltrumiteflight.util.ViltrumiteFlightPlayer;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("chaosticspells_tests")
@PrefixGameTestTemplate(false)
public final class AveViltrumiteGameTests {
    @GameTest(template = "empty", batch = "ave_creative")
    public static void vanillaCreativeFlightSurvivesCustomFlightStopAndTick(GameTestHelper helper) {
        for (UUID uuid : new UUID[]{OwnersWhipItem.OWNER_UUID,UUID.randomUUID()}) {
            ServerPlayer player=player(helper,uuid);
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            ViltrumiteIntegration.synchronize(player);
            player.getAbilities().flying=true;
            for(int i=0;i<12;i++) {
                flight(player).stopFlight();
                player.tick();
                helper.assertTrue(player.getAbilities().mayfly && player.getAbilities().flying,
                        "Inactive custom flight must not turn off vanilla creative flying, tick="+i);
            }
            helper.assertTrue(flight(player).getFlightState()==FlightState.NONE,"Vanilla flight must not enable Viltrumite flight");
            player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            flight(player).stopFlight();
            helper.assertTrue(player.getAbilities().flying,"Stopping custom flight must preserve spectator flight");
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            player.getAbilities().flying=true;
            flight(player).stopFlight();
            helper.assertTrue(!player.getAbilities().flying,"Survival must still lose unauthorized custom flight");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ave_1")
    public static void everyoneStartsHumanAndRaceCannotBeForged(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OwnersWhipItem.OWNER_UUID);
        ServerPlayer stranger = player(helper, UUID.randomUUID());
        helper.assertTrue(!core(owner).isViltrumite() && !core(stranger).isViltrumite(), "Both players must start human");
        helper.assertTrue(core(owner).hasChosenRace() && core(stranger).hasChosenRace(), "No player may be prompted to select a race");
        core(owner).setViltrumite(true);
        stranger.getPersistentData().putBoolean(AveViltrumiteAccess.ENABLED_TAG, true);
        core(stranger).setViltrumite(true);
        flight(stranger).setFlightState(FlightState.SONIC);
        helper.assertTrue(!core(owner).isViltrumite(), "The owner's race must require the command opt-in");
        helper.assertTrue(!core(stranger).isViltrumite() && flight(stranger).getFlightState() == FlightState.NONE,
                "Matching name, forged persistent opt-in and direct setters must not grant a different UUID powers");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ave_2")
    public static void ownerCommandWorksWithoutOpAndIsHiddenFromOthers(GameTestHelper helper) throws Exception {
        ServerPlayer owner = player(helper, OwnersWhipItem.OWNER_UUID);
        ServerPlayer stranger = player(helper, UUID.randomUUID());
        var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
        var ownSource = owner.createCommandSourceStack().withPermission(0);
        var otherSource = stranger.createCommandSourceStack().withPermission(4);
        helper.assertTrue(dispatcher.getRoot().getChild("ave").canUse(ownSource), "AveMollan must have /ave race without operator status");
        helper.assertTrue(!dispatcher.getRoot().getChild("ave").canUse(otherSource), "Even a different operator UUID must not see /ave race");
        dispatcher.execute("ave race", ownSource);
        helper.assertTrue(core(owner).isViltrumite() && owner.getAbilities().mayfly, "The owner command must enable powers and flight");
        helper.assertTrue(dispatcher.getRoot().getChild("viltrumite").canUse(ownSource), "Active owner may use cosmetic/config commands");
        helper.assertTrue(!dispatcher.getRoot().getChild("viltrumite").getChild("power").canUse(ownSource), "Legacy grant commands must not bypass /ave race");
        dispatcher.execute("ave race", ownSource);
        helper.assertTrue(!core(owner).isViltrumite() && !owner.getAbilities().mayfly, "The same command must return the owner to human");
        helper.assertTrue(!dispatcher.getRoot().getChild("viltrumite").canUse(ownSource), "Human owner must not expose the native command tree");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ave_3")
    public static void unauthorizedNetworkActionsAreStoppedBeforeDispatch(GameTestHelper helper) {
        ServerPlayer stranger = player(helper, UUID.randomUUID());
        stranger.connection.handleCustomPayload(new ServerboundCustomPayloadPacket(new RaceChoiceC2SPacket(true)));
        stranger.connection.handleCustomPayload(new ServerboundCustomPayloadPacket(new DashToggleC2SPacket()));
        stranger.connection.handleCustomPayload(new ServerboundCustomPayloadPacket(FlightToggleC2SPacket.INSTANCE));
        helper.assertTrue(!core(stranger).isViltrumite() && !core(stranger).isDashing()
                && flight(stranger).getFlightState() == FlightState.NONE, "Unauthorized C2S packets must not grant actions");
        ServerPlayer owner = player(helper, OwnersWhipItem.OWNER_UUID);
        ViltrumiteIntegration.changeRace(owner, true);
        helper.assertTrue(AveViltrumiteAccess.allowPayload(owner, ResourceLocation.parse("viltrumitecore:punch")), "Active owner may send ability input");
        helper.assertTrue(!AveViltrumiteAccess.allowPayload(owner, RaceChoiceC2SPacket.TYPE.id()), "Race choice packets must stay blocked even for the owner");
        ViltrumiteIntegration.changeRace(owner, false);
        helper.assertTrue(!AveViltrumiteAccess.allowPayload(owner, ResourceLocation.parse("viltrumiteflight:flight_toggle")), "Human owner may not activate Viltrumite flight");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ave_4")
    public static void humanModeClearsFlightAbilitiesAndOnlyModAttributes(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OwnersWhipItem.OWNER_UUID);
        ViltrumiteIntegration.changeRace(owner, true);
        core(owner).setBlocking(true);
        core(owner).setBarraging(true);
        core(owner).setPunchTicks(20);
        flight(owner).setFlightState(FlightState.SONIC);
        var attribute = owner.getAttribute(Attributes.ATTACK_DAMAGE);
        var coreModifier = ResourceLocation.parse("viltrumitecore:base_strength");
        var otherModifier = ResourceLocation.parse("chaosticspells:test_other_bonus");
        attribute.addTransientModifier(new AttributeModifier(coreModifier, 19, AttributeModifier.Operation.ADD_VALUE));
        attribute.addTransientModifier(new AttributeModifier(otherModifier, 2, AttributeModifier.Operation.ADD_VALUE));
        ViltrumiteIntegration.changeRace(owner, false);
        helper.assertTrue(!core(owner).isBlocking() && !core(owner).isBarraging() && core(owner).getPunchTicks() == 0,
                "Human mode must clear active abilities");
        helper.assertTrue(flight(owner).getFlightState() == FlightState.NONE && !owner.getAbilities().flying && !owner.getAbilities().mayfly,
                "Human mode must stop both custom and vanilla flying");
        helper.assertTrue(!attribute.hasModifier(coreModifier) && attribute.hasModifier(otherModifier),
                "Only Viltrumite attribute bonuses may be removed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ave_5")
    public static void raceChoiceSurvivesSaveLoadAndDeathClone(GameTestHelper helper) {
        ServerPlayer owner = player(helper, OwnersWhipItem.OWNER_UUID);
        ViltrumiteIntegration.changeRace(owner, true);
        CompoundTag saved = owner.saveWithoutId(new CompoundTag());
        ServerPlayer loaded = player(helper, OwnersWhipItem.OWNER_UUID);
        loaded.load(saved);
        ViltrumiteIntegration.synchronize(loaded);
        helper.assertTrue(AveViltrumiteAccess.isActive(loaded), "The explicit race opt-in must survive save/load");
        ServerPlayer respawned = player(helper, OwnersWhipItem.OWNER_UUID);
        NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(respawned, loaded, true));
        helper.assertTrue(AveViltrumiteAccess.isActive(respawned), "The owner race choice must survive a death clone");
        ViltrumiteIntegration.changeRace(respawned, false);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ave_6")
    public static void nonOwnersKeepVanillaCosmeticsAndRejectVirus(GameTestHelper helper) {
        ServerPlayer stranger = player(helper, UUID.randomUUID());
        var cosmetics = (dev.baranhan.viltrumitecore.util.ViltrumiteCosmeticsPlayer) stranger;
        cosmetics.setViltrumiteSkin("invincible");
        cosmetics.setViltrumiteCape("omniman");
        helper.assertTrue(cosmetics.getViltrumiteSkin().equals("off") && cosmetics.getViltrumiteCape().equals("off"),
                "Old cosmetic data must not replace another player's appearance");
        stranger.addEffect(new MobEffectInstance(ViltrumiteEffects.SCOURGE_VIRUS, 200));
        helper.assertTrue(!stranger.hasEffect(ViltrumiteEffects.SCOURGE_VIRUS), "The virus must not infect non-owners");
        helper.assertTrue(!AveViltrumiteAccess.allowScreen("dev.baranhan.viltrumitecore.client.gui.ViltrumiteDashboardScreen", false)
                && !AveViltrumiteAccess.allowScreen("dev.baranhan.viltrumiteflight.client.gui.FirstPersonScreen", false)
                && !AveViltrumiteAccess.allowScreen("dev.baranhan.viltrumitecore.client.gui.RaceSelectionScreen", true)
                && AveViltrumiteAccess.allowScreen("net.minecraft.client.gui.screens.inventory.InventoryScreen", false),
                "Human players must retain vanilla screens while all Viltrumite menus and the race picker stay hidden");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ave_7")
    public static void automaticRaidsAndMeteorsNeverTargetOtherPlayers(GameTestHelper helper) {
        ServerPlayer stranger = player(helper, UUID.randomUUID());
        var level = helper.getLevel();
        level.addNewPlayer(stranger);
        float oldEventChance = ViltrumiteCoreConfig.INSTANCE.worldEventChancePercent;
        float oldMeteorChance = ViltrumiteCoreConfig.INSTANCE.meteorSpawnChancePercent;
        try {
            ViltrumiteCoreConfig.INSTANCE.worldEventChancePercent = 100;
            ViltrumiteCoreConfig.INSTANCE.meteorSpawnChancePercent = 100;
            int events = WorldEventManager.get(level).activeEvents().size();
            int meteors = level.getEntitiesOfClass(MeteorEntity.class, stranger.getBoundingBox().inflate(512)).size();
            CoreEvents.onLevelTick(new LevelTickEvent.Post(() -> true, level));
            MeteorSpawnEvent.onLevelTick(new LevelTickEvent.Post(() -> true, level));
            helper.assertTrue(WorldEventManager.get(level).activeEvents().size() == events
                    && level.getEntitiesOfClass(MeteorEntity.class, stranger.getBoundingBox().inflate(512)).size() == meteors,
                    "Even 100% random-event configuration must not spawn global Viltrumite content");
        } finally {
            ViltrumiteCoreConfig.INSTANCE.worldEventChancePercent = oldEventChance;
            ViltrumiteCoreConfig.INSTANCE.meteorSpawnChancePercent = oldMeteorChance;
            level.removePlayerImmediately(stranger, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, UUID uuid) {
        GameProfile profile = new GameProfile(uuid, "AveMollan");
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile, ClientInformation.createDefault());
        player.connection = FakePlayerFactory.get(helper.getLevel(), profile).connection;
        player.moveTo(helper.absolutePos(new BlockPos(1, 1, 1)).getBottomCenter());
        return player;
    }

    private static ViltrumiteCorePlayer core(ServerPlayer player) { return (ViltrumiteCorePlayer) player; }
    private static ViltrumiteFlightPlayer flight(ServerPlayer player) { return (ViltrumiteFlightPlayer) player; }
}

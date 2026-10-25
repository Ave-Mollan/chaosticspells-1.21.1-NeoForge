package com.avemollan.chaosticspells.gametest;

import com.avemollan.chaosticspells.events.MissionaryHatEventHandler;
import com.avemollan.chaosticspells.entity.RaisedBabySkeleton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.avemollan.chaosticspells.item.MagicNightmareArmorItem;
import com.avemollan.chaosticspells.registry.ChaosticEntityRegistry;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.mojang.authlib.GameProfile;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.nio.file.Files;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;


@Mod("chaosticspells_tests")
@GameTestHolder("chaosticspells_tests")
@PrefixGameTestTemplate(false)
public class CompatibilityGameTests {
    @GameTest(template = "empty")
    public static void allBornInChaosMixinTargetsTransform(GameTestHelper helper) throws ClassNotFoundException {
        String[] procedures = {
                "StormcallersHornPriZaviershieniiIspolzovaniiaProcedure",
                "StaffoftheSummonerPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure",
                "StaffofMagicArrowsPriIspolzovaniiStrielkovoghoPriedmietaProcedure",
                "FrostbittenBladePriShchielchkiePKMProcedure",
                "PumpkinPistolKoghdaSnariadPopadaietVSushchnostProcedure",
                "DeathTotemKoghdaPriedmietNakhoditsiaVRukieProcedure",
                "NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure",
                "NightmareScytheKoghdaZhivaiaSushchnostPopadaietSPomoshchiuInstrumientaProcedure",
                "SoulSaberKoghdaZhivaiaSushchnostPopadaietSPomoshchiuInstrumientaProcedure",
                "DarkRitualDaggerPProcedure",
                "DarkMetalArmorSobytiieTaktovShliemaProcedure"
        };
        for (String procedure : procedures) {
            Class.forName("net.mcreator.borninchaosv.procedures." + procedure);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void boostedManaNeverExceedsCapacity(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.OBSESSION, 200));
        assertMana(helper, player, 99.0f, 100.0f);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void depletionScalesIncrementBeforeCapacityClamp(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200));
        assertMana(helper, player, 99.0f, 100.0f);
        assertMana(helper, player, 50.0f, 52.5f);
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200, 1));
        assertMana(helper, player, 50.0f, 50.0f);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void manaEffectsCombine(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        assertMana(helper, player, 50.0f, 55.0f);
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.OBSESSION, 200));
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.FURIOUS_RAMPAGE, 200));
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200));
        assertMana(helper, player, 50.0f, 53.59375f);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nightmareAdvancementDoesNotRequireDepletion(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        equipNightmare(player);
        tickChest(player);
        var advancement = player.server.getAdvancements().get(ResourceLocation.parse("born_in_chaos_v1:unitywith_darknessachievement"));
        helper.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "Full magic nightmare set must grant the Born in Chaos advancement without depletion");
        helper.assertTrue(!player.hasEffect(MobEffects.DAMAGE_RESISTANCE), "Resistance requires magic depletion");
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200));
        tickChest(player);
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE), "Full set must grant resistance during depletion");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mixedNightmareSetWithMagicChestRetainsBonus(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        equipNightmare(player);
        equip(player, EquipmentSlot.HEAD, BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_HELMET.get());
        equip(player, EquipmentSlot.LEGS, BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_LEGGINGS.get());
        equip(player, EquipmentSlot.FEET, BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_BOOTS.get());
        helper.assertTrue(MagicNightmareArmorItem.hasFullSet(player), "Mixed nightmare armor must count as a complete set");
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200));
        tickChest(player);
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE), "Mixed set with a magic chest must grant resistance");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mixedNightmareSetWithOriginalChestRetainsBonus(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        equipNightmare(player);
        equip(player, EquipmentSlot.CHEST, BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_CHESTPLATE.get());
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200));
        NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure.execute(player);
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE), "Born in Chaos must recognize magic nightmare pieces");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void darkMetalBonusesAndAdvancement(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        equip(player, EquipmentSlot.HEAD, ChaosticItemRegistry.CROWNED_MAGIC_DARK_METAL_HELMET.get());
        equip(player, EquipmentSlot.CHEST, ChaosticItemRegistry.MAGIC_DARK_METAL_CHESTPLATE.get());
        equip(player, EquipmentSlot.LEGS, ChaosticItemRegistry.MAGIC_DARK_METAL_LEGGINGS.get());
        equip(player, EquipmentSlot.FEET, ChaosticItemRegistry.MAGIC_DARK_METAL_BOOTS.get());
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);
        player.setHealth(11);
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        tickChest(player);
        helper.assertTrue(!player.hasEffect(MobEffects.WITHER), "Magic dark metal must clear wither");
        helper.assertTrue(player.hasEffect(BornInChaosV1ModMobEffects.RAMPANT_RAMPAGE), "Rampage must use 30% of maximum health");
        helper.assertTrue(player.hasEffect(MobEffects.HUNGER), "Low-health bonus must apply hunger");
        var advancement = player.server.getAdvancements().get(ResourceLocation.parse("born_in_chaos_v1:chaos_knight"));
        helper.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "Magic dark metal must grant chaos_knight");
        player.removeEffect(BornInChaosV1ModMobEffects.RAMPANT_RAMPAGE);
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.RAMPANT_RAMPAGE, 50));
        tickChest(player);
        helper.assertTrue(player.getEffect(BornInChaosV1ModMobEffects.RAMPANT_RAMPAGE).getDuration() == 100,
                "Low-health bonus must refresh while the activation condition holds");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void missionaryHatTriggersOnlyForMonsterAttacks(GameTestHelper helper) {
        ServerPlayer victim = player(helper);
        helper.setBlock(1, 1, 1, net.minecraft.world.level.block.Blocks.AIR);
        equip(victim, EquipmentSlot.HEAD, ChaosticItemRegistry.MISSIONARY_HAT_HELMET.get());
        ServerPlayer attacker = player(helper);
        MissionaryHatEventHandler.onIncomingDamage(new LivingIncomingDamageEvent(victim,
                new DamageContainer(victim.damageSources().playerAttack(attacker), 1)));
        helper.assertTrue(!victim.hasEffect(BornInChaosV1ModMobEffects.MAGIC_DEPLETION),
                "Player attacks must not activate the missionary hat");
        Zombie monster = new Zombie(EntityType.ZOMBIE, helper.getLevel());
        MissionaryHatEventHandler.onIncomingDamage(new LivingIncomingDamageEvent(victim,
                new DamageContainer(victim.damageSources().mobAttack(monster), 1)));
        helper.assertTrue(victim.hasEffect(BornInChaosV1ModMobEffects.MAGIC_DEPLETION),
                "Monster attacks must activate the missionary hat cooldown");
        var summons = helper.getLevel().getEntities(ChaosticEntityRegistry.RAISED_BABY_SKELETON.get(),
                victim.getBoundingBox().inflate(2), entity -> SummonManager.getSummons(victim).contains(entity.getUUID()));
        helper.assertTrue(summons.size() == 1, "The missionary hat must create one owned skeleton; found "
                + summons.size() + ", tracked " + SummonManager.getSummons(victim).size()
                + ", feet block " + victim.level().getBlockState(victim.blockPosition()));
        MissionaryHatEventHandler.onIncomingDamage(new LivingIncomingDamageEvent(victim,
                new DamageContainer(victim.damageSources().mobAttack(monster), 1)));
        helper.assertTrue(helper.getLevel().getEntities(ChaosticEntityRegistry.RAISED_BABY_SKELETON.get(),
                victim.getBoundingBox().inflate(2), entity -> SummonManager.getSummons(victim).contains(entity.getUUID())).size() == 1,
                "Magic depletion must prevent repeated summons");
        summons.forEach(net.minecraft.world.entity.Entity::discard);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allRecipesAndChaosUpgradeLoad(GameTestHelper helper) {
        String[] recipes = {"bonescaller_staff", "chaos_rune", "chaos_staff", "chaos_transformation_amulet",
                "chaos_upgrade_orb", "crowned_magic_dark_metal_helmet", "magic_dark_metal_boots",
                "magic_dark_metal_chestplate", "magic_dark_metal_helmet", "magic_dark_metal_leggings",
                "magic_nightmare_boots", "magic_nightmare_chestplate", "magic_nightmare_helmet",
                "magic_nightmare_leggings", "missionary_hat_helmet"};
        for (String recipe : recipes) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("chaosticspells", recipe)).isPresent(),
                    "Recipe did not load: " + recipe);
        }
        helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(
                        io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY)
                        .containsKey(ResourceLocation.parse("irons_spellbooks:chaos_power")),
                "Chaos upgrade orb type must load into Iron's data registry");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void babySkeletonKeepsLegAnimationWhileChasingTarget(GameTestHelper helper) throws Exception {
        RaisedBabySkeleton skeleton = new RaisedBabySkeleton(ChaosticEntityRegistry.RAISED_BABY_SKELETON.get(), helper.getLevel());
        var animationFile = ModList.get().getModFileById("born_in_chaos_v1").getFile()
                .findResource("assets", "born_in_chaos_v1", "animations", "babyskeleton2.animation.json");
        JsonObject animations = JsonParser.parseString(Files.readString(animationFile)).getAsJsonObject().getAsJsonObject("animations");
        skeleton.setTarget(new Zombie(EntityType.ZOMBIE, helper.getLevel()));
        skeleton.setAggressive(true);
        RawAnimation combatMovement = requestedMovement(skeleton, true, 0.5f);
        String clip = combatMovement.getAnimationStages().getFirst().animationName();
        helper.assertTrue(animations.has(clip), "Combat locomotion references a missing Born in Chaos animation: " + clip);
        helper.assertTrue(clip.equals("walk"), "Chasing a target must keep the skeleton's walking/running cycle");
        JsonObject walkBones = animations.getAsJsonObject(clip).getAsJsonObject("bones");
        for (String leg : new String[]{"left_leg", "right_leg"}) {
            JsonObject rotation = walkBones.getAsJsonObject(leg).getAsJsonObject("rotation");
            long poses = rotation.entrySet().stream().map(entry -> entry.getValue().toString()).distinct().count();
            helper.assertTrue(poses > 1, "Locomotion must contain changing leg poses: " + leg);
            helper.assertTrue(!animations.getAsJsonObject("attack").getAsJsonObject("bones").has(leg),
                    "Arm attack animation must not replace leg locomotion");
        }
        helper.assertTrue(requestedMovement(skeleton, false, 0.2f).equals(RawAnimation.begin().thenLoop("walk")),
                "Limb motion must keep locomotion running when the renderer's moving flag briefly drops");
        helper.assertTrue(requestedMovement(skeleton, false, 0.0f).equals(RawAnimation.begin().thenLoop("idle")),
                "An aggressive skeleton that is stationary must idle");
        skeleton.setAggressive(false);
        helper.assertTrue(requestedMovement(skeleton, true, 0.5f).equals(RawAnimation.begin().thenLoop("walk")),
                "Normal following movement must keep its walk cycle");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void babySkeletonDeathAnimationTakesPriorityOverMovement(GameTestHelper helper) throws Exception {
        RaisedBabySkeleton skeleton = new RaisedBabySkeleton(ChaosticEntityRegistry.RAISED_BABY_SKELETON.get(), helper.getLevel());
        skeleton.setHealth(0);
        helper.assertTrue(requestedMovement(skeleton, true, 0.5f).equals(RawAnimation.begin().thenPlay("death")),
                "Residual movement must not replace the death animation");
        helper.succeed();
    }

    private static RawAnimation requestedMovement(RaisedBabySkeleton skeleton, boolean moving, float limbAmount) throws Exception {
        RecordingAnimationController controller = new RecordingAnimationController(skeleton);
        AnimationState<RaisedBabySkeleton> state = new AnimationState<>(skeleton, 1.0f, limbAmount, 0.0f, moving)
                .withController(controller);
        var predicate = RaisedBabySkeleton.class.getDeclaredMethod("movementPredicate", AnimationState.class);
        predicate.setAccessible(true);
        if (predicate.invoke(skeleton, state) != PlayState.CONTINUE || controller.requested == null) {
            throw new AssertionError("Locomotion controller did not request an animation");
        }
        return controller.requested;
    }

    private static final class RecordingAnimationController extends AnimationController<RaisedBabySkeleton> {
        private RawAnimation requested;

        private RecordingAnimationController(RaisedBabySkeleton skeleton) {
            super(skeleton, "movement", 4, state -> PlayState.CONTINUE);
        }

        @Override
        public void setAnimation(RawAnimation animation) {
            // Capture the real predicate's request without loading a client renderer on the test server.
            requested = animation;
        }
    }

    private static ServerPlayer player(GameTestHelper helper) {
        // A real ServerPlayer is needed for advancements. A no-op connection avoids firing login
        // events that require a real client to complete the mods' network handshake.
        GameProfile profile = new GameProfile(UUID.randomUUID(), "CompatTest");
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile,
                ClientInformation.createDefault());
        player.connection = FakePlayerFactory.get(helper.getLevel(), profile).connection;
        player.moveTo(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1)).getBottomCenter());
        player.getAttribute(AttributeRegistry.MAX_MANA).setBaseValue(100);
        player.getAttribute(AttributeRegistry.MANA_REGEN).setBaseValue(5);
        return player;
    }

    private static void assertMana(GameTestHelper helper, ServerPlayer player, float initial, float expected) {
        MagicData data = MagicData.getPlayerMagicData(player);
        data.setMana(initial);
        new MagicManager().regenPlayerMana(player, data);
        helper.assertTrue(Math.abs(data.getMana() - expected) < 0.001f,
                "Expected mana " + expected + ", got " + data.getMana());
    }

    private static void equipNightmare(ServerPlayer player) {
        equip(player, EquipmentSlot.HEAD, ChaosticItemRegistry.MAGIC_NIGHTMARE_HELMET.get());
        equip(player, EquipmentSlot.CHEST, ChaosticItemRegistry.MAGIC_NIGHTMARE_CHESTPLATE.get());
        equip(player, EquipmentSlot.LEGS, ChaosticItemRegistry.MAGIC_NIGHTMARE_LEGGINGS.get());
        equip(player, EquipmentSlot.FEET, ChaosticItemRegistry.MAGIC_NIGHTMARE_BOOTS.get());
    }

    private static void equip(ServerPlayer player, EquipmentSlot slot, Item item) {
        player.setItemSlot(slot, new ItemStack(item));
    }

    private static void tickChest(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        chest.getItem().inventoryTick(chest, player.level(), player, 0, false);
    }
}

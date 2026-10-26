package com.avemollan.chaosticspells;

import com.avemollan.chaosticspells.client.BonescallerStaffClientExtensions;
import com.avemollan.chaosticspells.client.ChaosTransformationAmuletItemColors;
import com.avemollan.chaosticspells.client.ChaosStaffClientExtensions;
import com.avemollan.chaosticspells.client.ChaoticArrowRenderer;
import com.avemollan.chaosticspells.client.MagicArrowRenderer;
import com.avemollan.chaosticspells.client.PumpkinBarrageRenderer;
import com.avemollan.chaosticspells.client.RaisedBabySkeletonRenderer;
import com.avemollan.chaosticspells.client.RaisedFallenChaosKnightRenderer;
import com.avemollan.chaosticspells.client.MagicDarkMetalArmorClientExtensions;
import com.avemollan.chaosticspells.client.MagicNightmareArmorClientExtensions;
import com.avemollan.chaosticspells.client.MissionaryHatClientExtensions;
import com.avemollan.chaosticspells.client.model.CrownedMagicDarkMetalHelmetModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalBootModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalChestplateModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalHelmetModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalLeggingsModel;
import com.avemollan.chaosticspells.client.model.MagicNightmareBootModel;
import com.avemollan.chaosticspells.client.model.MagicNightmareChestplateModel;
import com.avemollan.chaosticspells.client.model.MagicNightmareHelmetModel;
import com.avemollan.chaosticspells.client.model.MagicNightmareLeggingsModel;
import com.avemollan.chaosticspells.client.model.MissionaryHatModel;
import com.avemollan.chaosticspells.client.model.PumpkinBlProjectileModel;
import com.avemollan.chaosticspells.registry.ChaosticEntityRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;

@Mod(value = ChaosticSpells.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ChaosticSpells.MODID, value = Dist.CLIENT)
public class ChaosticSpellsClient {
    private static final String BORN_IN_CHAOS_MOD_ID = "born_in_chaos_v1";
    private static final ResourceLocation MAGIC_DEPLETION_RL =
            ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_MOD_ID, "magic_depletion");
    private static final ResourceLocation OBSESSION_RL =
            ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_MOD_ID, "obsession");
    private static final ResourceLocation FURIOUS_RAMPAGE_RL =
            ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_MOD_ID, "furious_rampage");

    public ChaosticSpellsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void appendBornInChaosIronsManaRegenCompatLine(GatherEffectScreenTooltipsEvent event) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_MOD_ID)) {
            return;
        }

        MobEffectInstance inst = event.getEffectInstance();
        ResourceLocation id = inst.getEffect().unwrapKey().map(key -> key.location()).orElse(null);
        if (id == null) {
            return;
        }

        Component line = null;
        int amplifier = inst.getAmplifier();
        if (MAGIC_DEPLETION_RL.equals(id)) {
            line = Component.translatable(
                            "chaosticspells.compat.born_in_chaos.tooltip.irons_mana_regen_penalty",
                            50 * (amplifier + 1))
                    .withStyle(ChatFormatting.BLUE);
        } else if (OBSESSION_RL.equals(id)) {
            line = Component.translatable(
                            "chaosticspells.compat.born_in_chaos.tooltip.irons_mana_regen_bonus",
                            25 + 10 * amplifier)
                    .withStyle(ChatFormatting.BLUE);
        } else if (FURIOUS_RAMPAGE_RL.equals(id)) {
            line = Component.translatable(
                            "chaosticspells.compat.born_in_chaos.tooltip.irons_mana_regen_bonus",
                            15 + 10 * amplifier)
                    .withStyle(ChatFormatting.BLUE);
        }
        if (line != null) {
            event.getTooltip().add(line);
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        if (com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess.available()) {
            event.enqueueWork(com.avemollan.chaosticspells.compat.viltrumite.ViltrumiteClientPolicy::install);
        }
        ChaosticSpells.LOGGER.info("ChaoticSpells client setup");
        ChaosticSpells.LOGGER.debug("User: {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void filterViltrumiteCreativeItems(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if (com.avemollan.chaosticspells.compat.viltrumite.AveViltrumiteAccess.available()) {
            com.avemollan.chaosticspells.compat.viltrumite.ViltrumiteClientPolicy.creativeTab(event);
        }
    }

    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ChaosTransformationAmuletItemColors.register(event);
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new com.avemollan.chaosticspells.client.OwnersWhipClientExtensions(), ChaosticItemRegistry.OWNERS_WHIP.get());
        event.registerItem(new BonescallerStaffClientExtensions(), ChaosticItemRegistry.BONESCALLER_STAFF.get());
        event.registerItem(new ChaosStaffClientExtensions(), ChaosticItemRegistry.CHAOS_STAFF.get());
        event.registerItem(MissionaryHatClientExtensions.INSTANCE, ChaosticItemRegistry.MISSIONARY_HAT_HELMET.get());
        event.registerItem(MagicNightmareArmorClientExtensions.HELMET, ChaosticItemRegistry.MAGIC_NIGHTMARE_HELMET.get());
        event.registerItem(MagicNightmareArmorClientExtensions.CHESTPLATE, ChaosticItemRegistry.MAGIC_NIGHTMARE_CHESTPLATE.get());
        event.registerItem(MagicNightmareArmorClientExtensions.LEGGINGS, ChaosticItemRegistry.MAGIC_NIGHTMARE_LEGGINGS.get());
        event.registerItem(MagicNightmareArmorClientExtensions.BOOTS, ChaosticItemRegistry.MAGIC_NIGHTMARE_BOOTS.get());
        event.registerItem(MagicDarkMetalArmorClientExtensions.HELMET, ChaosticItemRegistry.MAGIC_DARK_METAL_HELMET.get());
        event.registerItem(MagicDarkMetalArmorClientExtensions.CROWNED_HELMET, ChaosticItemRegistry.CROWNED_MAGIC_DARK_METAL_HELMET.get());
        event.registerItem(MagicDarkMetalArmorClientExtensions.CHESTPLATE, ChaosticItemRegistry.MAGIC_DARK_METAL_CHESTPLATE.get());
        event.registerItem(MagicDarkMetalArmorClientExtensions.LEGGINGS, ChaosticItemRegistry.MAGIC_DARK_METAL_LEGGINGS.get());
        event.registerItem(MagicDarkMetalArmorClientExtensions.BOOTS, ChaosticItemRegistry.MAGIC_DARK_METAL_BOOTS.get());
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PumpkinBlProjectileModel.LAYER_LOCATION, PumpkinBlProjectileModel::createBodyLayer);
        event.registerLayerDefinition(MissionaryHatModel.LAYER_LOCATION, MissionaryHatModel::createBodyLayer);
        event.registerLayerDefinition(MagicNightmareHelmetModel.LAYER_LOCATION, MagicNightmareHelmetModel::createBodyLayer);
        event.registerLayerDefinition(MagicNightmareChestplateModel.LAYER_LOCATION, MagicNightmareChestplateModel::createBodyLayer);
        event.registerLayerDefinition(MagicNightmareLeggingsModel.LAYER_LOCATION, MagicNightmareLeggingsModel::createBodyLayer);
        event.registerLayerDefinition(MagicNightmareBootModel.LAYER_LOCATION, MagicNightmareBootModel::createBodyLayer);
        event.registerLayerDefinition(MagicDarkMetalHelmetModel.LAYER_LOCATION, MagicDarkMetalHelmetModel::createBodyLayer);
        event.registerLayerDefinition(CrownedMagicDarkMetalHelmetModel.LAYER_LOCATION, CrownedMagicDarkMetalHelmetModel::createBodyLayer);
        event.registerLayerDefinition(MagicDarkMetalChestplateModel.LAYER_LOCATION, MagicDarkMetalChestplateModel::createBodyLayer);
        event.registerLayerDefinition(MagicDarkMetalLeggingsModel.LAYER_LOCATION, MagicDarkMetalLeggingsModel::createBodyLayer);
        event.registerLayerDefinition(MagicDarkMetalBootModel.LAYER_LOCATION, MagicDarkMetalBootModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ChaosticEntityRegistry.CHAOTIC_ARROW.get(), ChaoticArrowRenderer::new);
        event.registerEntityRenderer(ChaosticEntityRegistry.MAGIC_ARROW.get(), MagicArrowRenderer::new);
        event.registerEntityRenderer(ChaosticEntityRegistry.PUMPKIN_BARRAGE_PROJECTILE.get(), PumpkinBarrageRenderer::new);
        event.registerEntityRenderer(ChaosticEntityRegistry.RAISED_FALLEN_CHAOS_KNIGHT.get(), RaisedFallenChaosKnightRenderer::new);
        event.registerEntityRenderer(ChaosticEntityRegistry.RAISED_BABY_SKELETON.get(), RaisedBabySkeletonRenderer::new);
    }
}

package com.avemollan.chaosticspells;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import com.avemollan.chaosticspells.events.ChaosTransformationAmuletHandler;
import com.avemollan.chaosticspells.events.DeathPactEventHandler;
import com.avemollan.chaosticspells.events.MissionaryHatEventHandler;
import com.avemollan.chaosticspells.registry.ChaosticArmorMaterialRegistry;
import com.avemollan.chaosticspells.registry.ChaosticAttributeRegistry;
import com.avemollan.chaosticspells.registry.ChaosticEntityRegistry;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.avemollan.chaosticspells.registry.ChaosticMobEffectRegistry;
import com.avemollan.chaosticspells.registry.ChaosticSchoolRegistry;
import com.avemollan.chaosticspells.registry.ChaosticSpellRegistry;

import net.neoforged.neoforge.common.NeoForge;

import software.bernie.geckolib.animatable.GeoItem;

@Mod(ChaosticSpells.MODID)
public class ChaosticSpells {
    public static final String MODID = "chaosticspells";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChaosticSpells(IEventBus modEventBus, ModContainer modContainer) {
        ChaosticArmorMaterialRegistry.register(modEventBus);
        ChaosticAttributeRegistry.register(modEventBus);
        ChaosticMobEffectRegistry.register(modEventBus);
        ChaosticSchoolRegistry.register(modEventBus);
        ChaosticSpellRegistry.register(modEventBus);
        ChaosticEntityRegistry.register(modEventBus);
        ChaosticItemRegistry.register(modEventBus);

        modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            GeoItem.registerSyncedAnimatable(ChaosticItemRegistry.BONESCALLER_STAFF.get());
            GeoItem.registerSyncedAnimatable(ChaosticItemRegistry.CHAOS_STAFF.get());
        }));

        NeoForge.EVENT_BUS.register(DeathPactEventHandler.class);
        NeoForge.EVENT_BUS.register(ChaosTransformationAmuletHandler.class);
        NeoForge.EVENT_BUS.register(MissionaryHatEventHandler.class);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, Config.SPEC);
    }
}

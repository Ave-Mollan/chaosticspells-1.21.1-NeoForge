package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.entity.ChaoticArrowProjectile;
import com.avemollan.chaosticspells.entity.MagicArrowProjectile;
import com.avemollan.chaosticspells.entity.PumpkinBarrageProjectile;
import com.avemollan.chaosticspells.entity.RaisedBabySkeleton;
import com.avemollan.chaosticspells.entity.RaisedFallenChaosKnight;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChaosticEntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ChaosticSpells.MODID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        eventBus.addListener(ChaosticEntityRegistry::registerAttributes);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RAISED_FALLEN_CHAOS_KNIGHT.get(), RaisedFallenChaosKnight.createAttributes().build());
        event.put(RAISED_BABY_SKELETON.get(), RaisedBabySkeleton.createAttributes().build());
    }

    public static final DeferredHolder<EntityType<?>, EntityType<ChaoticArrowProjectile>> CHAOTIC_ARROW =
            ENTITIES.register("chaotic_arrow", () -> EntityType.Builder.<ChaoticArrowProjectile>of(ChaoticArrowProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "chaotic_arrow").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MagicArrowProjectile>> MAGIC_ARROW =
            ENTITIES.register("magic_arrow", () -> EntityType.Builder.<MagicArrowProjectile>of(MagicArrowProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "magic_arrow").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PumpkinBarrageProjectile>> PUMPKIN_BARRAGE_PROJECTILE =
            ENTITIES.register("pumpkin_barrage_projectile", () -> EntityType.Builder.<PumpkinBarrageProjectile>of(PumpkinBarrageProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "pumpkin_barrage_projectile").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<RaisedFallenChaosKnight>> RAISED_FALLEN_CHAOS_KNIGHT =
            ENTITIES.register("raised_fallen_chaos_knight", () -> EntityType.Builder.<RaisedFallenChaosKnight>of(RaisedFallenChaosKnight::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.5F)
                    .clientTrackingRange(75)
                    .build(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "raised_fallen_chaos_knight").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<RaisedBabySkeleton>> RAISED_BABY_SKELETON =
            ENTITIES.register("raised_baby_skeleton", () -> EntityType.Builder.<RaisedBabySkeleton>of(RaisedBabySkeleton::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.3F)
                    .clientTrackingRange(75)
                    .build(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "raised_baby_skeleton").toString()));

    private ChaosticEntityRegistry() {
    }
}

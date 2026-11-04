package com.avemollan.chaosticspells.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Bakes each armor model layer once per resource reload instead of once per rendered frame.
 *
 * <p>Entity Model Features stops replacing a layer after it has been baked 64 times outside a reload,
 * so per-frame baking (the MCreator pattern) makes EMF resource-pack models revert after about a second.
 * Cleared on {@code EntityRenderersEvent.AddLayers}, which runs after every entity model reload.
 */
public final class ArmorModelCache {
    private static final Map<ModelLayerLocation, ModelPart> CACHE = new ConcurrentHashMap<>();

    private ArmorModelCache() {}

    public static ModelPart bake(ModelLayerLocation layer) {
        return bake(Minecraft.getInstance().getEntityModels(), layer);
    }

    public static ModelPart bake(EntityModelSet models, ModelLayerLocation layer) {
        return CACHE.computeIfAbsent(layer, models::bakeLayer);
    }

    public static void clear() {
        CACHE.clear();
    }
}

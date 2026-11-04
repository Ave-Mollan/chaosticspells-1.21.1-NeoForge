package com.avemollan.chaosticspells.mixin.compat.bicdarkmetal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;

import com.avemollan.chaosticspells.client.ArmorModelCache;

/**
 * Born in Chaos Dark Metal helmet ($1), chestplate ($2) and boots ($3) client extensions bake their model layer on
 * every rendered frame. Routes those bakes through {@link ArmorModelCache} so Entity Model Features keeps applying
 * resource-pack models (it ignores layers baked more than 64 times) and the per-frame allocation disappears.
 * {@code require = 0}: if a Born in Chaos update renumbers the classes, the original rendering is kept.
 */
@Mixin(
        targets = {
            "net.mcreator.borninchaosv.item.DarkMetalArmorItem$1",
            "net.mcreator.borninchaosv.item.DarkMetalArmorItem$2",
            "net.mcreator.borninchaosv.item.DarkMetalArmorItem$3"
        })
public class BcDarkMetalArmorModelCacheMixin {

    @Redirect(
            method = "getHumanoidArmorModel",
            require = 0,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/geom/EntityModelSet;bakeLayer(Lnet/minecraft/client/model/geom/ModelLayerLocation;)Lnet/minecraft/client/model/geom/ModelPart;"))
    private ModelPart chaosticspells$bakeOnce(EntityModelSet models, ModelLayerLocation layer) {
        return ArmorModelCache.bake(models, layer);
    }
}

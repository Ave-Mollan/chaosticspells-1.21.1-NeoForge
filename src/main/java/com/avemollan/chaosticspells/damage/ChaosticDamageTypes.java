package com.avemollan.chaosticspells.damage;

import com.avemollan.chaosticspells.ChaosticSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ChaosticDamageTypes {
    public static final ResourceKey<DamageType> CHAOS_MAGIC =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "chaos_magic"));

    private ChaosticDamageTypes() {
    }
}

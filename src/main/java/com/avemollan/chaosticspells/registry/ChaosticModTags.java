package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ChaosticModTags {
    public static final TagKey<Item> CHAOS_FOCUS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "chaos_focus"));

    private ChaosticModTags() {
    }
}

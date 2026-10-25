package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.damage.ChaosticDamageTypes;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ChaosticSchoolRegistry {
    private static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, ChaosticSpells.MODID);

    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }

    public static final ResourceLocation CHAOS_RESOURCE = ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "chaos");

    private static Supplier<SchoolType> registerSchool(SchoolType schoolType) {
        return SCHOOLS.register(schoolType.getId().getPath(), () -> schoolType);
    }

    public static final Supplier<SchoolType> CHAOS = registerSchool(new SchoolType(
            CHAOS_RESOURCE,
            ChaosticModTags.CHAOS_FOCUS,
            Component.translatable("school.chaosticspells.chaos").withStyle(Style.EMPTY.withColor(0x955f5f)),
            ChaosticAttributeRegistry.CHAOS_SPELL_POWER,
            ChaosticAttributeRegistry.CHAOS_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ChaosticDamageTypes.CHAOS_MAGIC
    ));

    private ChaosticSchoolRegistry() {
    }
}

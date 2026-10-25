package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChaosticAttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, ChaosticSpells.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
        eventBus.addListener(ChaosticAttributeRegistry::modifyEntityAttributes);
    }

    public static final DeferredHolder<Attribute, Attribute> CHAOS_MAGIC_RESIST = newResistanceAttribute("chaos");
    public static final DeferredHolder<Attribute, Attribute> CHAOS_SPELL_POWER = newPowerAttribute("chaos");

    private static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(holder -> e.add(entity, holder)));
    }

    private static DeferredHolder<Attribute, Attribute> newResistanceAttribute(String id) {
        return ATTRIBUTES.register(id + "_magic_resist",
                () -> new MagicPercentAttribute("attribute.chaosticspells." + id + "_magic_resist", 1.0D, -100, 100).setSyncable(true));
    }

    private static DeferredHolder<Attribute, Attribute> newPowerAttribute(String id) {
        return ATTRIBUTES.register(id + "_spell_power",
                () -> new MagicPercentAttribute("attribute.chaosticspells." + id + "_spell_power", 1.0D, -100, 100).setSyncable(true));
    }

    private ChaosticAttributeRegistry() {
    }
}

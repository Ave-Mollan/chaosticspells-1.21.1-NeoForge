package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.item.curio.ChaosTransformationAmulet;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.avemollan.chaosticspells.util.SchoolTypePowerAttributeAccess;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public final class ChaosTransformationAmuletItemColors {

    private static final int DEFAULT_UNBOUND_OVERLAY = 0xFFCB1E27;

    private ChaosTransformationAmuletItemColors() {
    }

    public static int overlayTint(ItemStack stack, int layer) {
        if (layer != 1) {
            return 0xFFFFFFFF;
        }
        if (Minecraft.getInstance().level == null) {
            return ChaosTransformationAmulet.getBoundAttributeLocation(stack).isEmpty() ? DEFAULT_UNBOUND_OVERLAY : 0xFFFFFFFF;
        }
        var reg = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        var boundOpt = ChaosTransformationAmulet.getBoundAttributeLocation(stack);
        if (boundOpt.isEmpty()) {
            return DEFAULT_UNBOUND_OVERLAY;
        }
        Holder.Reference<Attribute> attrRef = reg.getHolder(ResourceKey.create(Registries.ATTRIBUTE, boundOpt.get())).orElse(null);
        if (attrRef == null) {
            return 0xFFFFFFFF;
        }
        SchoolType school = SchoolTypePowerAttributeAccess.findSchoolForPowerAttribute(SchoolRegistry.REGISTRY, attrRef);
        if (school == null) {
            return 0xFFFFFFFF;
        }
        Vector3f rgb = school.getTargetingColor();
        int r = (int) (rgb.x * 255);
        int g = (int) (rgb.y * 255);
        int b = (int) (rgb.z * 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Registers item color handler (call from {@link ChaosticSpellsClient}). */
    public static void register(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> overlayTint(stack, tintIndex), ChaosticItemRegistry.CHAOS_TRANSFORMATION_AMULET.get());
    }
}

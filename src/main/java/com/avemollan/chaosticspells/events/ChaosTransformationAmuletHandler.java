package com.avemollan.chaosticspells.events;

import com.avemollan.chaosticspells.item.curio.ChaosTransformationAmulet;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.avemollan.chaosticspells.util.SchoolTypePowerAttributeAccess;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class ChaosTransformationAmuletHandler {

    private ChaosTransformationAmuletHandler() {
    }

    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        Holder<Attribute> schoolPower = SchoolTypePowerAttributeAccess.getPowerAttribute(event.getSchoolType());

        CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                inv.findFirstCurio(ChaosticItemRegistry.CHAOS_TRANSFORMATION_AMULET.get()).ifPresent(result -> {
                    ItemStack stack = result.stack();
                    var boundLoc = ChaosTransformationAmulet.getBoundAttributeLocation(stack);
                    if (boundLoc.isEmpty()) {
                        return;
                    }
                    Holder<Attribute> boundHolder = player.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE)
                            .getHolder(ResourceKey.create(Registries.ATTRIBUTE, boundLoc.get()))
                            .orElse(null);
                    if (boundHolder == null || !boundHolder.equals(schoolPower)) {
                        return;
                    }
                    int damage = stack.getDamageValue();
                    int charges = ChaosTransformationAmulet.MAX_CHARGES - damage;
                    if (charges <= 0) {
                        return;
                    }
                    stack.setDamageValue(damage + 1);
                    ChaosTransformationAmulet.updateModifier(player, stack);
                }));
    }
}

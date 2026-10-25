package com.avemollan.chaosticspells.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

public class MissionaryHatHelmetItem extends ArmorItem {

    private static final String BORN_IN_CHAOS = "born_in_chaos_v1";

    /** Matches BiC: {@code item.born_in_chaos_v1.missionary_hat_helmet.description_0} split by newlines */
    private static final String BIO_DESCRIPTION_KEY = "item.born_in_chaos_v1.missionary_hat_helmet.description_0";

    private static final String BIO_SHIFT_KEY = "item.born_in_chaos_v1.missionary_hat_helmet.shift";

    private static final ResourceLocation ARMOR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/missionaryhat.png");

    public MissionaryHatHelmetItem(Holder<ArmorMaterial> material, Item.Properties props) {
        super(material, Type.HELMET, props);
    }

    private static boolean bornInChaosPresent() {
        return ModList.get().isLoaded(BORN_IN_CHAOS);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (bornInChaosPresent()) {
            return Component.translatable("item.born_in_chaos_v1.missionary_hat_helmet");
        }
        return Component.translatable(stack.getDescriptionId()).withStyle(ChatFormatting.GOLD);
    }

    @Override
    public ResourceLocation getArmorTexture(
            ItemStack stack,
            Entity entity,
            EquipmentSlot slot,
            ArmorMaterial.Layer armorMaterialLayer,
            boolean innerTexture) {
        return ARMOR_TEXTURE;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
        if (bornInChaosPresent()) {
            if (!tooltipFlag.hasShiftDown()) {
                showShiftPrompt(tooltip);
                return;
            }
            String hover = Language.getInstance().getOrDefault(BIO_DESCRIPTION_KEY);
            if (hover != null) {
                for (String line : hover.split("\\n")) {
                    if (!line.isEmpty()) {
                        tooltip.add(Component.literal(line));
                    }
                }
            }
            return;
        }
        ChatFormatting lore = ChatFormatting.DARK_GRAY;
        if (tooltipFlag.hasShiftDown()) {
            tooltip.add(Component.translatable("item.chaosticspells.missionary_hat_helmet.description_0").withStyle(lore));
            tooltip.add(Component.translatable("item.chaosticspells.missionary_hat_helmet.description_1").withStyle(lore));
        } else {
            tooltip.add(Component.translatable("item.chaosticspells.missionary_hat_helmet.shift").withStyle(lore));
        }
    }

    /** Shift hint: Born in Chaos line if loaded in language, otherwise ChaosticSpells tooltip. */
    private static void showShiftPrompt(List<Component> tooltip) {
        Language lang = Language.getInstance();
        if (lang.has(BIO_SHIFT_KEY)) {
            tooltip.add(Component.translatable(BIO_SHIFT_KEY));
        } else {
            tooltip.add(Component.translatable("item.chaosticspells.missionary_hat_helmet.shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

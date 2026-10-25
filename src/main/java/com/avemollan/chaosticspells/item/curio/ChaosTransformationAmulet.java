package com.avemollan.chaosticspells.item.curio;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.registry.ChaosticAttributeRegistry;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Optional;

public class ChaosTransformationAmulet extends Item implements ICurioItem {

    public static final String NBT_ATTRIBUTE_KEY = "BoundAttribute";
    public static final int MAX_CHARGES = 50;
    private static final int MANA_PER_CHARGE = 50;

    public static final ResourceLocation TRANSFER_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "chaos_power_transfer");

    public ChaosTransformationAmulet() {
        super(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1).durability(MAX_CHARGES));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasBoundAttribute(stack) || super.isFoil(stack);
    }

    private static boolean hasBoundAttribute(ItemStack stack) {
        return getBoundAttributeLocation(stack).isPresent();
    }

    public static Optional<ResourceLocation> getBoundAttributeLocation(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return Optional.empty();
        }
        var tag = data.copyTag();
        if (!tag.contains(NBT_ATTRIBUTE_KEY)) {
            return Optional.empty();
        }
        return Optional.of(ResourceLocation.parse(tag.getString(NBT_ATTRIBUTE_KEY)));
    }

    private static void setBoundAttribute(ItemStack stack, ResourceLocation attributeId) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(NBT_ATTRIBUTE_KEY, attributeId.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        int currentCharges = MAX_CHARGES - stack.getDamageValue();
        return currentCharges < MAX_CHARGES;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int charges = MAX_CHARGES - stack.getDamageValue();
        return Math.round(13.0F * charges / MAX_CHARGES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xAA00FF;
    }

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD.value(), 1.0f, 1.0f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack mainHandStack = player.getItemInHand(hand);
        ItemStack offHandStack = player.getOffhandItem();

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(mainHandStack);
        }

        var orbKey = offHandStack.get(ComponentRegistry.UPGRADE_ORB_TYPE);
        if (orbKey != null && offHandStack.getItem() instanceof UpgradeOrbItem) {
            Registry<UpgradeOrbType> orbReg = level.registryAccess().registryOrThrow(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY);
            Optional<Holder.Reference<UpgradeOrbType>> orbHolder = orbReg.getHolder(orbKey);
            if (orbHolder.isEmpty()) {
                return InteractionResultHolder.pass(mainHandStack);
            }
            Holder<Attribute> targetAttribute = orbHolder.get().value().attribute();
            if (targetAttribute.is(ChaosticAttributeRegistry.CHAOS_SPELL_POWER)) {
                if (!level.isClientSide()) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                return InteractionResultHolder.fail(mainHandStack);
            }
            ResourceLocation attrId = BuiltInRegistries.ATTRIBUTE.getKey(targetAttribute.value());
            if (attrId == null) {
                return InteractionResultHolder.pass(mainHandStack);
            }
            setBoundAttribute(mainHandStack, attrId);
            if (!level.isClientSide()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            return InteractionResultHolder.sidedSuccess(mainHandStack, level.isClientSide());
        }

        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(mainHandStack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            int currentCharges = MAX_CHARGES - mainHandStack.getDamageValue();
            if (currentCharges >= MAX_CHARGES) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.5f, 1.5f);
                return InteractionResultHolder.fail(mainHandStack);
            }

            MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
            float currentMana = magicData.getMana();
            if (currentMana < MANA_PER_CHARGE) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.5f, 1.5f);
                return InteractionResultHolder.fail(mainHandStack);
            }

            magicData.setMana(currentMana - MANA_PER_CHARGE);
            mainHandStack.setDamageValue(mainHandStack.getDamageValue() - 1);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7f, 1.2f);

            return InteractionResultHolder.success(mainHandStack);
        }

        return InteractionResultHolder.fail(mainHandStack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return slotContext.entity() instanceof Player p && p.isShiftKeyDown();
    }

    public static void updateModifier(LivingEntity entity, ItemStack stack) {
        Optional<ResourceLocation> boundOpt = getBoundAttributeLocation(stack);
        if (boundOpt.isEmpty()) {
            return;
        }

        int currentCharges = MAX_CHARGES - stack.getDamageValue();

        Holder<Attribute> targetHolder = entity.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE)
                .getHolder(ResourceKey.create(Registries.ATTRIBUTE, boundOpt.get()))
                .orElse(null);
        if (targetHolder == null) {
            return;
        }

        AttributeInstance targetAttr = entity.getAttribute(targetHolder);
        if (targetAttr == null) {
            return;
        }

        targetAttr.removeModifier(TRANSFER_MODIFIER_ID);

        if (currentCharges <= 0) {
            return;
        }

        AttributeInstance chaosAttr = entity.getAttribute(ChaosticAttributeRegistry.CHAOS_SPELL_POWER);
        if (chaosAttr == null) {
            return;
        }

        double chaosBaseValue = chaosAttr.getBaseValue();
        double chaosTotal = chaosAttr.getValue();
        double chaosPercentageBonus = chaosBaseValue != 0 ? (chaosTotal / chaosBaseValue) - 1.0 : 0;

        if (chaosPercentageBonus > 0) {
            AttributeModifier modifier = new AttributeModifier(
                    TRANSFER_MODIFIER_ID,
                    chaosPercentageBonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            targetAttr.addTransientModifier(modifier);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide() || entity.tickCount % 20 != 0) {
            return;
        }
        updateModifier(entity, stack);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide()) {
            updateModifier(entity, stack);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Optional<ResourceLocation> boundOpt = getBoundAttributeLocation(stack);
        if (boundOpt.isEmpty()) {
            return;
        }
        Holder<Attribute> targetHolder = entity.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE)
                .getHolder(ResourceKey.create(Registries.ATTRIBUTE, boundOpt.get()))
                .orElse(null);
        if (targetHolder == null) {
            return;
        }
        AttributeInstance targetAttr = entity.getAttribute(targetHolder);
        if (targetAttr != null) {
            targetAttr.removeModifier(TRANSFER_MODIFIER_ID);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("curios.modifiers.necklace").withStyle(ChatFormatting.GOLD));

        Optional<ResourceLocation> boundLoc = getBoundAttributeLocation(stack);
        if (boundLoc.isPresent()) {
            Attribute attr = BuiltInRegistries.ATTRIBUTE.get(boundLoc.get());
            if (attr != null) {
                tooltip.add(Component.translatable(
                                "item.chaosticspells.chaos_transformation_amulet.bound",
                                Component.translatable(attr.getDescriptionId()))
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.add(Component.translatable("item.chaosticspells.chaos_transformation_amulet.desc").withStyle(ChatFormatting.YELLOW));
            }
        } else {
            tooltip.add(Component.translatable("item.chaosticspells.chaos_transformation_amulet.desc").withStyle(ChatFormatting.YELLOW));
        }

        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }

    @Override
    public Component getName(ItemStack stack) {
        Optional<ResourceLocation> boundLoc = getBoundAttributeLocation(stack);
        if (boundLoc.isEmpty()) {
            return super.getName(stack);
        }
        Attribute attr = BuiltInRegistries.ATTRIBUTE.get(boundLoc.get());
        if (attr == null) {
            return super.getName(stack);
        }
        MutableComponent base = Component.translatable(getDescriptionId(stack));
        return base.append(Component.literal(" ("))
                .append(Component.translatable(attr.getDescriptionId()))
                .append(Component.literal(")"));
    }
}

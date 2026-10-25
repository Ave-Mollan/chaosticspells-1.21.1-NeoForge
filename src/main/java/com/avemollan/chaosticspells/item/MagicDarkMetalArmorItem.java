package com.avemollan.chaosticspells.item;

import java.util.List;

import com.google.common.collect.Iterables;

import net.minecraft.core.Holder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

import net.neoforged.fml.ModList;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.registry.ChaosticArmorMaterialRegistry;
import com.avemollan.chaosticspells.registry.ChaosticAttributeRegistry;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;

/**
 * Ports legacy compat Magic Dark Metal: full-set removes Wither and (with Born in Chaos loaded) grants
 * {@code rampant_rampage} / Hunger near low health ({@literal <=}30% max HP like the old compat behaviour).
 */
public abstract class MagicDarkMetalArmorItem extends ArmorItem {

    private static final String BORN_IN_CHAOS_ID = "born_in_chaos_v1";

    private static final ResourceKey<net.minecraft.world.effect.MobEffect> RAMPANT_RAMPAGE_KEY =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "rampant_rampage"));

    protected MagicDarkMetalArmorItem(Type type, Properties properties) {
        super(ChaosticArmorMaterialRegistry.MAGIC_DARK_METAL, type, properties.attributes(createAttributeModifiers(type)));
    }

    public static Properties propertiesFor(Type type) {
        int durability = switch (type) {
            case BOOTS -> 650;
            case LEGGINGS -> 750;
            case CHESTPLATE -> 800;
            case HELMET -> 550;
            default -> type.getDurability(50);
        };
        return new Properties().durability(durability);
    }

    private static ItemAttributeModifiers createAttributeModifiers(Type type) {
        Holder<ArmorMaterial> material = ChaosticArmorMaterialRegistry.MAGIC_DARK_METAL;
        int defense = material.value().getDefense(type);
        float toughness = material.value().toughness();
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
        ResourceLocation armorKey = ResourceLocation.withDefaultNamespace("armor." + type.getName());

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(armorKey, defense, AttributeModifier.Operation.ADD_VALUE),
                slotGroup);
        builder.add(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(armorKey, toughness, AttributeModifier.Operation.ADD_VALUE),
                slotGroup);

        float kb = material.value().knockbackResistance();
        if (kb > 0.0F) {
            builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(armorKey, kb, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup);
        }

        String slotId = type.getSlot().getSerializedName();
        for (AttributeContainer holder : spellBonuses()) {
            builder.add(holder.attribute(), holder.createModifier(slotId), slotGroup);
        }

        return builder.build();
    }

    /** Legacy ExtendedArmorMaterials.MAGIC_DARK_METAL mana / chaos / spell bonuses. */
    private static AttributeContainer[] spellBonuses() {
        return new AttributeContainer[]{
                new AttributeContainer(AttributeRegistry.MAX_MANA, 75, AttributeModifier.Operation.ADD_VALUE),
                new AttributeContainer(ChaosticAttributeRegistry.CHAOS_SPELL_POWER, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new AttributeContainer(AttributeRegistry.SPELL_POWER, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
        };
    }

    public static boolean isDarkMetalHelmetPiece(ItemStack stack) {
        return stack.is(ChaosticItemRegistry.MAGIC_DARK_METAL_HELMET.get())
                || stack.is(ChaosticItemRegistry.CROWNED_MAGIC_DARK_METAL_HELMET.get());
    }

    public static boolean hasFullSet(Player player) {
        return isDarkMetalHelmetPiece(player.getItemBySlot(EquipmentSlot.HEAD))
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ChaosticItemRegistry.MAGIC_DARK_METAL_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ChaosticItemRegistry.MAGIC_DARK_METAL_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ChaosticItemRegistry.MAGIC_DARK_METAL_BOOTS.get());
    }

    private static void tickFullSet(Player player) {
        if (player.level().isClientSide() || !hasFullSet(player)) {
            return;
        }
        if (player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
        }

        if (player instanceof ServerPlayer serverPlayer && ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            AdvancementHolder advancement = serverPlayer.server.getAdvancements().get(
                    ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "chaos_knight"));
            if (advancement != null) {
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                for (String criterion : progress.getRemainingCriteria()) {
                    serverPlayer.getAdvancements().award(advancement, criterion);
                }
            }
        }

        float maxHealth = player.getMaxHealth();
        float threshold = maxHealth * 0.3f;
        if (player.getHealth() <= threshold) {
            if (ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
                player.level()
                        .registryAccess()
                        .registryOrThrow(Registries.MOB_EFFECT)
                        .getHolder(RAMPANT_RAMPAGE_KEY)
                        .ifPresent(rampHolder -> player.addEffect(new MobEffectInstance(rampHolder, 100, 0)));
            }
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 2, false, false));
        }
    }

    private static ResourceLocation armorTexturePath(Type type) {
        String path = switch (type) {
            case HELMET -> "textures/entities/magicdarkmetalhelmet.png";
            case CHESTPLATE -> "textures/entities/magicdarkmetalchestplate.png";
            case LEGGINGS -> "textures/entities/magicdarkmetalleggins.png";
            case BOOTS -> "textures/entities/magicdarkmetalboots.png";
            default -> "textures/entities/magicdarkmetalhelmet.png";
        };
        return ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, path);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerTexture) {
        return armorTexturePath(getType());
    }

    protected void appendDarkMetalTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (flag.hasShiftDown()) {
            tooltip.add(Component.translatable("item.chaosticspells.magic_dark_metal_armor.set_bonus_0"));
            tooltip.add(Component.translatable("item.chaosticspells.magic_dark_metal_armor.set_bonus_1"));
            tooltip.add(Component.translatable("item.chaosticspells.magic_dark_metal_armor.set_bonus_2"));
        } else {
            tooltip.add(Component.translatable("item.chaosticspells.magic_dark_metal_armor.shift"));
        }
    }

    public static final class CrownedHelmet extends MagicDarkMetalArmorItem implements IPresetSpellContainer {
        private static final ResourceLocation ARMOR_TEX =
                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "textures/entities/crownedmagicdarkmetalhelmet.png");

        public CrownedHelmet() {
            super(Type.HELMET, propertiesFor(Type.HELMET));
        }

        @Override
        public ResourceLocation getArmorTexture(
                ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerTexture) {
            return ARMOR_TEX;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            if (flag.hasShiftDown()) {
                tooltip.add(Component.translatable("item.chaosticspells.crowned_magic_dark_metal_helmet.description_0"));
                tooltip.add(Component.translatable("item.chaosticspells.crowned_magic_dark_metal_helmet.description_1"));
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("item.chaosticspells.magic_dark_metal_armor.set_bonus_0"));
                tooltip.add(Component.translatable("item.chaosticspells.magic_dark_metal_armor.set_bonus_1"));
                tooltip.add(Component.translatable("item.chaosticspells.magic_dark_metal_armor.set_bonus_2"));
            } else {
                tooltip.add(Component.translatable("item.chaosticspells.crowned_magic_dark_metal_helmet.shift"));
            }
        }

        @Override
        public void initializeSpellContainer(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return;
            }
            if (!ISpellContainer.isSpellContainer(stack)) {
                ISpellContainer.set(stack, ISpellContainer.create(1, true, true));
            }
        }
    }

    public static final class Helmet extends MagicDarkMetalArmorItem {
        public Helmet() {
            super(Type.HELMET, propertiesFor(Type.HELMET));
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            this.appendDarkMetalTooltip(stack, context, tooltip, flag);
        }
    }

    public static final class Chestplate extends MagicDarkMetalArmorItem implements IPresetSpellContainer {
        public Chestplate() {
            super(Type.CHESTPLATE, propertiesFor(Type.CHESTPLATE));
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
            super.inventoryTick(stack, level, entity, slotId, selected);
            if (!level.isClientSide() && entity instanceof Player player && Iterables.contains(player.getArmorSlots(), stack)) {
                tickFullSet(player);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            this.appendDarkMetalTooltip(stack, context, tooltip, flag);
        }

        @Override
        public void initializeSpellContainer(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return;
            }
            if (!ISpellContainer.isSpellContainer(stack)) {
                ISpellContainer.set(stack, ISpellContainer.create(1, true, true));
            }
        }
    }

    public static final class Leggings extends MagicDarkMetalArmorItem {
        public Leggings() {
            super(Type.LEGGINGS, propertiesFor(Type.LEGGINGS));
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            this.appendDarkMetalTooltip(stack, context, tooltip, flag);
        }
    }

    public static final class Boots extends MagicDarkMetalArmorItem {
        public Boots() {
            super(Type.BOOTS, propertiesFor(Type.BOOTS));
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            this.appendDarkMetalTooltip(stack, context, tooltip, flag);
        }
    }
}

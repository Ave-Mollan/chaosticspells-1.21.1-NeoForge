package com.avemollan.chaosticspells.item;

import java.util.List;

import com.google.common.collect.Iterables;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import com.avemollan.chaosticspells.compat.BornInChaosNightmareArmorCompatBridge;
import com.avemollan.chaosticspells.registry.ChaosticArmorMaterialRegistry;
import com.avemollan.chaosticspells.registry.ChaosticAttributeRegistry;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;

/**
 * Legacy compat nightmare robe line + Iron mana attributes; helmet darkness logic matches Born in Chaos
 * {@code NightmareMantleoftheNightKazhdyiTikDliaShliemaProcedure}, chestplate full-set handling matches
 * {@code NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure}. Tooltips match original compat lang (§ formatting).
 */
public abstract class MagicNightmareArmorItem extends ArmorItem {

    private static final String BORN_IN_CHAOS_ID = "born_in_chaos_v1";

    private static final ResourceKey<net.minecraft.world.effect.MobEffect> MAGIC_DEPLETION_KEY =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "magic_depletion"));

    private static final ResourceKey<net.minecraft.world.effect.MobEffect> UNITY_WITH_DARKNESS_KEY =
            ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "unity_with_darkness"));

    private static final ResourceLocation UNITY_WITH_DARKNESS_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "unitywith_darknessachievement");

    protected MagicNightmareArmorItem(Type type, Properties properties) {
        super(ChaosticArmorMaterialRegistry.MAGIC_NIGHTMARE, type, properties.attributes(createAttributeModifiers(type)));
    }

    public static Properties propertiesFor(Type type) {
        int durability = switch (type) {
            case BOOTS -> 390;
            case LEGGINGS -> 450;
            case CHESTPLATE -> 480;
            case HELMET -> 330;
            default -> type.getDurability(25);
        };
        return new Properties().durability(durability);
    }

    private static ItemAttributeModifiers createAttributeModifiers(Type type) {
        Holder<ArmorMaterial> material = ChaosticArmorMaterialRegistry.MAGIC_NIGHTMARE;
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

    private static AttributeContainer[] spellBonuses() {
        return new AttributeContainer[]{
                new AttributeContainer(AttributeRegistry.MAX_MANA, 75, AttributeModifier.Operation.ADD_VALUE),
                new AttributeContainer(ChaosticAttributeRegistry.CHAOS_SPELL_POWER, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new AttributeContainer(AttributeRegistry.SPELL_POWER, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
        };
    }

    public static boolean hasFullSet(Player player) {
        return BornInChaosNightmareArmorCompatBridge.isNightmarePiece(player.getItemBySlot(EquipmentSlot.HEAD).getItem(), EquipmentSlot.HEAD)
                && BornInChaosNightmareArmorCompatBridge.isNightmarePiece(player.getItemBySlot(EquipmentSlot.CHEST).getItem(), EquipmentSlot.CHEST)
                && BornInChaosNightmareArmorCompatBridge.isNightmarePiece(player.getItemBySlot(EquipmentSlot.LEGS).getItem(), EquipmentSlot.LEGS)
                && BornInChaosNightmareArmorCompatBridge.isNightmarePiece(player.getItemBySlot(EquipmentSlot.FEET).getItem(), EquipmentSlot.FEET);
    }

    /** Mirrors NightmareMantleoftheNightKazhdyiTikDliaShliemaProcedure (BiC). Requires Born in Chaos loaded. */
    private static void tickHelmetUnityDarkness(Player player) {
        if (!ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        Level level = player.level();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        if (level.getMaxLocalRawBrightness(BlockPos.containing(x, y, z)) > 4) {
            return;
        }
        level.registryAccess()
                .registryOrThrow(Registries.MOB_EFFECT)
                .getHolder(UNITY_WITH_DARKNESS_KEY)
                .ifPresent(holder -> player.addEffect(new MobEffectInstance(holder, 20, 1, false, false)));
        if (player.isSprinting() && level instanceof ServerLevel serverLevel) {
            BuiltInRegistries.PARTICLE_TYPE
                    .getOptional(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS_ID, "dark_smoke"))
                    .filter(SimpleParticleType.class::isInstance)
                    .map(SimpleParticleType.class::cast)
                    .ifPresent(pt -> serverLevel.sendParticles(pt, x, y + 1.0, z, 1, 0.1, 0.1, 0.1, 0.1));
        }
    }

    /** Mirrors NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure (BiC). Requires Born in Chaos loaded. */
    private static void tickChestplateFullSet(Player player) {
        if (!hasFullSet(player) || !ModList.get().isLoaded(BORN_IN_CHAOS_ID)) {
            return;
        }
        boolean magicDepleted = player.registryAccess()
                .registryOrThrow(Registries.MOB_EFFECT)
                .getHolder(MAGIC_DEPLETION_KEY)
                .map(player::hasEffect)
                .orElse(false);
        if (magicDepleted) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 1, false, false));
        }
        if (player instanceof ServerPlayer serverPlayer) {
            grantUnityAdvancementIfPossible(serverPlayer);
        }
    }

    private static void grantUnityAdvancementIfPossible(ServerPlayer player) {
        AdvancementHolder advancement = player.server.getAdvancements().get(UNITY_WITH_DARKNESS_ADVANCEMENT);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criteria : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criteria);
        }
    }

    private static ResourceLocation armorTexturePath(Type type) {
        String path = switch (type) {
            case HELMET -> "textures/entities/magicnightrobemasc.png";
            case CHESTPLATE -> "textures/entities/magicnightrobebreastplate.png";
            case LEGGINGS -> "textures/entities/magicnightrobeleggings.png";
            case BOOTS -> "textures/entities/magicnightrobeboot.png";
            default -> "textures/entities/magicnightrobemasc.png";
        };
        return ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, path);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerTexture) {
        return armorTexturePath(getType());
    }

    protected void appendNightmareTooltip(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, boolean helmetExtraLine) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (flag.hasShiftDown()) {
            if (helmetExtraLine) {
                tooltip.add(Component.translatable("item.chaosticspells.magic_nightmare_helmet.bonus"));
                tooltip.add(Component.empty());
            }
            tooltip.add(Component.translatable("item.chaosticspells.magic_nightmare_armor.set_bonus"));
        } else {
            tooltip.add(Component.translatable("item.chaosticspells.magic_nightmare_armor.shift"));
        }
    }

    public static final class Helmet extends MagicNightmareArmorItem {
        public Helmet() {
            super(Type.HELMET, propertiesFor(Type.HELMET));
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
            super.inventoryTick(stack, level, entity, slotId, selected);
            if (!level.isClientSide() && entity instanceof Player player && Iterables.contains(player.getArmorSlots(), stack)) {
                tickHelmetUnityDarkness(player);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            appendNightmareTooltip(stack, context, tooltip, flag, true);
        }
    }

    public static final class Chestplate extends MagicNightmareArmorItem implements IPresetSpellContainer {
        public Chestplate() {
            super(Type.CHESTPLATE, propertiesFor(Type.CHESTPLATE));
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
            super.inventoryTick(stack, level, entity, slotId, selected);
            if (!level.isClientSide() && entity instanceof Player player && Iterables.contains(player.getArmorSlots(), stack)) {
                tickChestplateFullSet(player);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            appendNightmareTooltip(stack, context, tooltip, flag, false);
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

    public static final class Leggings extends MagicNightmareArmorItem {
        public Leggings() {
            super(Type.LEGGINGS, propertiesFor(Type.LEGGINGS));
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            appendNightmareTooltip(stack, context, tooltip, flag, false);
        }
    }

    public static final class Boots extends MagicNightmareArmorItem {
        public Boots() {
            super(Type.BOOTS, propertiesFor(Type.BOOTS));
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            appendNightmareTooltip(stack, context, tooltip, flag, false);
        }
    }
}

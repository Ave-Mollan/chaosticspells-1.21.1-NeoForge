package com.avemollan.chaosticspells.item;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.registry.ChaosticAttributeRegistry;
import com.avemollan.chaosticspells.registry.ChaosticSpellRegistry;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class ChaosStaffItem extends StaffItem implements IPresetSpellContainer, GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ChaosStaffItem() {
        super(ItemPropertiesHelper.equipment(1).rarity(Rarity.UNCOMMON).attributes(createModifiers()));
    }

    private static ItemAttributeModifiers createModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        AttributeRegistry.MANA_REGEN,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "chaos_staff_mana_regen"),
                                0.15,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        EquipmentSlotGroup.MAINHAND)
                .add(
                        ChaosticAttributeRegistry.CHAOS_SPELL_POWER,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "chaos_staff_chaos_power"),
                                0.10,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void initializeSpellContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (!ISpellContainer.isSpellContainer(stack)) {
            var mutable = ISpellContainer.create(1, true, false).mutableCopy();
            AbstractSpell preset = ChaosticSpellRegistry.MAGIC_ARROW.get();
            mutable.addSpell(preset, 1, true);
            ISpellContainer.set(stack, mutable.toImmutable());
        }
    }

    @Override
    public boolean hasCustomRendering() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "idle", 20, ChaosStaffItem::idlePredicate));
    }

    private static PlayState idlePredicate(AnimationState<ChaosStaffItem> state) {
        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

package com.avemollan.chaosticspells.item;

import com.avemollan.chaosticspells.ChaosticSpells;
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

public final class BonescallerStaffItem extends StaffItem implements IPresetSpellContainer, GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BonescallerStaffItem() {
        super(ItemPropertiesHelper.equipment(1).rarity(Rarity.RARE).attributes(createModifiers()));
    }

    private static ItemAttributeModifiers createModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        AttributeRegistry.MAX_MANA,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "bonescaller_staff_max_mana"),
                                75,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(
                        AttributeRegistry.SUMMON_DAMAGE,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "bonescaller_staff_summon_damage"),
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
            AbstractSpell preset = ChaosticSpellRegistry.RAISE_BABY_SKELETON.get();
            mutable.addSpell(preset, 2, true);
            ISpellContainer.set(stack, mutable.toImmutable());
        }
    }

    @Override
    public boolean hasCustomRendering() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "idle", 20, BonescallerStaffItem::idlePredicate));
    }

    private static PlayState idlePredicate(AnimationState<BonescallerStaffItem> state) {
        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

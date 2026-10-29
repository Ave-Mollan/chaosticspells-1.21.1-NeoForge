package com.avemollan.chaosticspells.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Server authorization is deliberately fixed to the player name only; the UUID is ignored. */
public final class OwnersWhipItem extends Item implements GeoItem {
    public static final String OWNER_NAME = "AveMollan";
    public static final int MAX_CHARGE_TICKS = 40;
    public static final int COOLDOWN_TICKS = 12;
    public static final double REACH = 8.0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public OwnersWhipItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public static boolean isOwner(Entity entity) {
        return entity instanceof Player player && OWNER_NAME.equals(player.getGameProfile().getName());
    }

    /** The owner among the level's players, or null. */
    public static Player owner(Level level) {
        for (Player player : level.players()) if (isOwner(player)) return player;
        return null;
    }

    public static float damageForTicks(long ticks) {
        return 5.0f + 15.0f * Math.clamp(ticks, 0, MAX_CHARGE_TICKS) / MAX_CHARGE_TICKS;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel && isOwner(holder)) {
            GeoItem.getOrAssignId(stack, serverLevel);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.chaosticspells.owners_whip.owner").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.chaosticspells.owners_whip.controls").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.chaosticspells.owners_whip.damage").withStyle(ChatFormatting.RED));
        lines.add(Component.translatable("tooltip.chaosticspells.owners_whip.retained").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rope_pose", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.owners_whip.idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

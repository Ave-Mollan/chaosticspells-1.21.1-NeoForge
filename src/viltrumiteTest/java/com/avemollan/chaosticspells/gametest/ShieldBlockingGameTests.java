package com.avemollan.chaosticspells.gametest;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** ViltrumiteCore's Player#isBlocking override must not stop a raised shield from blocking (owner and others). */
@GameTestHolder("chaosticspells_tests")
@PrefixGameTestTemplate(false)
public final class ShieldBlockingGameTests {
    private static final UUID OWNER_UUID = UUID.fromString("516e7a70-c667-499b-9870-2783e9e7b214");

    @GameTest(template = "empty", batch = "ave_shield")
    public static void raisedShieldBlocksFrontalMeleeWithViltrumiteInstalled(GameTestHelper helper) {
        // A stranger and the owner (whip.11 knows the owner by UUID, later builds by name; this profile has both).
        GameProfile[] profiles = {new GameProfile(UUID.randomUUID(), "Stranger"), new GameProfile(OWNER_UUID, "AveMollan")};
        for (GameProfile profile : profiles) {
            ServerPlayer player = player(helper, profile);
            Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 1, 3));
            player.setYRot(0f);
            player.setYHeadRot(0f);

            helper.assertFalse(player.isDamageSourceBlocked(player.damageSources().mobAttack(zombie)),
                    "Without a raised shield nothing may be blocked");

            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
            player.startUsingItem(InteractionHand.OFF_HAND);
            raisedFor(player, 10);
            helper.assertTrue(player.isDamageSourceBlocked(player.damageSources().mobAttack(zombie)),
                    "A shield raised for 10 ticks must block a frontal zombie hit (" + player.getGameProfile().getName() + ")");

            zombie.discard();
        }
        helper.succeed();
    }

    /** Pretend the item has been in use for {@code ticks} ticks (vanilla needs 5 before a shield blocks). */
    private static void raisedFor(LivingEntity entity, int ticks) {
        try {
            Field f = LivingEntity.class.getDeclaredField("useItemRemaining");
            f.setAccessible(true);
            f.setInt(entity, entity.getUseItem().getUseDuration(entity) - ticks);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static ServerPlayer player(GameTestHelper helper, GameProfile profile) {
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile, ClientInformation.createDefault());
        player.connection = FakePlayerFactory.get(helper.getLevel(), profile).connection;
        player.moveTo(helper.absolutePos(new BlockPos(1, 1, 1)).getBottomCenter());
        return player;
    }
}

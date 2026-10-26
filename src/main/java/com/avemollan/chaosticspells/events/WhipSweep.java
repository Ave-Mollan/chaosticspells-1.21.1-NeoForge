package com.avemollan.chaosticspells.events;

import java.util.ArrayList;
import java.util.List;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative swept volume: an eight-block frontal arc, not the crosshair ray. */
public final class WhipSweep {
    public static final double HALF_ARC_DEGREES = 80;
    private static final double HALF_HEIGHT = 1.5;

    public static List<LivingEntity> targets(ServerPlayer player) {
        return targets(player, 0, HALF_ARC_DEGREES);
    }

    public static List<LivingEntity> targets(ServerPlayer player, double centerAngle, double halfWidth) {
        List<LivingEntity> result = new ArrayList<>();
        Vec3 origin = player.getEyePosition().add(0, -.35, 0);
        Vec3 forward = player.getLookAngle();
        double yaw = Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3 up = right.cross(forward).normalize();
        // ServerPlayer.canHarmPlayer already includes the server PvP rule and team friendly fire.
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(OwnersWhipItem.REACH + 1),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator() && entity.isPickable() && !(entity instanceof ArmorStand)
                        && entity.getRootVehicle() != player.getRootVehicle() && !entity.isAlliedTo(player) && !player.isAlliedTo(entity)
                        && !(entity instanceof OwnableEntity pet && player.getUUID().equals(pet.getOwnerUUID()))
                        && !(entity instanceof Player other && !player.canHarmPlayer(other)))) {
            var box = entity.getBoundingBox();
            Vec3 delta = box.getCenter().subtract(origin);
            double front = delta.dot(forward), sideways = delta.dot(right), height = delta.dot(up);
            double margin = Math.sqrt(box.getXsize() * box.getXsize() + box.getZsize() * box.getZsize()) * .5;
            double distance = Math.hypot(front, sideways);
            if (front + margin < 0 || distance - margin > OwnersWhipItem.REACH || Math.abs(height) > HALF_HEIGHT + box.getYsize() * .5) continue;
            double angle = Math.toDegrees(Math.atan2(sideways, front));
            double allowance = Math.toDegrees(Math.atan2(margin, Math.max(.1, distance)));
            if (Math.abs(angle) > HALF_ARC_DEGREES + allowance || Math.abs(angle - centerAngle) > halfWidth + allowance) continue;
            var wall = player.level().clip(new ClipContext(player.getEyePosition(), box.getCenter(),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (wall.getType() == HitResult.Type.MISS) result.add(entity);
        }
        return result;
    }

    private WhipSweep() {}
}

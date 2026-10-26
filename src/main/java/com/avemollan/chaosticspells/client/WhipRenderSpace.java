package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.ChaosticSpells;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Converts authoritative world points through the actual rendered hand matrix, including other model layers. */
@EventBusSubscriber(modid=ChaosticSpells.MODID,value=Dist.CLIENT)
public final class WhipRenderSpace {
    private static boolean ready;
    private static Vec3 camera=Vec3.ZERO;
    static Space current;
    public record Space(Matrix4f worldToRoot,Matrix4f rootToWorld,Vec3 camera) {
        public Vec3 local(Vec3 world){Vector3f p=new Vector3f((float)(world.x-camera.x),(float)(world.y-camera.y),(float)(world.z-camera.z));worldToRoot.transformPosition(p);return new Vec3(p.x,p.y,p.z);}
        public Vec3 world(float x,float y,float z){Vector3f p=new Vector3f(x,y,z);rootToWorld.transformPosition(p);return new Vec3(p.x+camera.x,p.y+camera.y,p.z+camera.z);}
    }
    @SubscribeEvent public static void capture(RenderLevelStageEvent e) {
        ready=true;camera=e.getCamera().getPosition();
    }
    static Space create(Matrix4f model,Matrix4f root) {
        if(!ready)return null;
        // LevelRenderer uses a fresh, unrotated entity PoseStack. Its camera view matrix is
        // applied by the shader, so multiplying it here would rotate the world rope twice.
        Matrix4f matrix=new Matrix4f(model).mul(root).invert();
        return new Space(matrix,new Matrix4f(matrix).invert(),camera);
    }
    private WhipRenderSpace(){}
}

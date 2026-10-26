package com.avemollan.chaosticspells.network;

import java.util.UUID;
import com.avemollan.chaosticspells.ChaosticSpells;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** One server-computed world rope is shared by every observer, including late trackers. */
public record WhipMotionPayload(UUID player,ResourceLocation dimension,long item,long tick,byte kind,long started,
        int peakA,int peakB,int duration,int side,boolean loose,double x,double y,double z,float[] points) implements CustomPacketPayload {
    public static final byte REST=0,CHARGE=1,ATTACK=2,HIDDEN=3;
    public static final Type<WhipMotionPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID,"whip_motion"));
    public WhipMotionPayload { if(points.length!=75) throw new IllegalArgumentException("Expected 25 rope points");points=points.clone(); }
    public static final StreamCodec<RegistryFriendlyByteBuf,WhipMotionPayload> CODEC=StreamCodec.of((b,p)->{
        b.writeUUID(p.player);b.writeResourceLocation(p.dimension);b.writeVarLong(p.item);b.writeLong(p.tick);b.writeByte(p.kind);b.writeLong(p.started);
        b.writeVarInt(p.peakA);b.writeVarInt(p.peakB);b.writeVarInt(p.duration);b.writeByte(p.side);b.writeBoolean(p.loose);
        b.writeDouble(p.x);b.writeDouble(p.y);b.writeDouble(p.z);for(float f:p.points)b.writeFloat(f);
    },b->{
        UUID player=b.readUUID();ResourceLocation dimension=b.readResourceLocation();long item=b.readVarLong(),tick=b.readLong();byte kind=b.readByte();long started=b.readLong();
        int a=b.readVarInt(),c=b.readVarInt(),d=b.readVarInt(),side=b.readByte();boolean loose=b.readBoolean();double x=b.readDouble(),y=b.readDouble(),z=b.readDouble();
        float[] points=new float[75];for(int i=0;i<75;i++) points[i]=b.readFloat();
        return new WhipMotionPayload(player,dimension,item,tick,kind,started,a,c,d,side,loose,x,y,z,points);
    });
    public static void register(RegisterPayloadHandlersEvent e) {
        e.registrar("whip-world-2").playToClient(TYPE,CODEC,(p,c)->c.enqueueWork(()->com.avemollan.chaosticspells.client.WhipClientMotion.accept(p)));
    }
    @Override public Type<WhipMotionPayload> type(){return TYPE;}
}

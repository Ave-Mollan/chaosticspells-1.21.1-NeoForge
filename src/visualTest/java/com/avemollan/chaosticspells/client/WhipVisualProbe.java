package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.network.WhipActionPayload;
import com.avemollan.chaosticspells.network.WhipMotionPayload;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import java.nio.file.*;
import java.util.*;
import java.net.InetAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Development-only two-client visual regression scene. Excluded from normal source sets and release JARs. */
@EventBusSubscriber(modid=ChaosticSpells.MODID,value=Dist.CLIENT)
public final class WhipVisualProbe {
    static final String ROLE=System.getProperty("whip.visual.role","host");
    static final Path OUT=Path.of(System.getProperty("whip.visual.output","build/whip7-visual-session"));
    static final boolean COMMANDS=Boolean.getBoolean("whip.visual.commands");
    static final String WORLD=COMMANDS?"ave-command-qa-8":"whip-visual-qa-7c";
    static int commandStarted=-1;
    static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> commandSuggestions;
    static final Map<Long,Integer> TRACE=new TreeMap<>();
    static final Set<String> SHOTS=new HashSet<>();
    static boolean initialized,opened,prepared,ready,started,finished;
    static int ticks,worldTicks,releaseAt=-1,doneTicks,frames;
    static long preparation=-1,testStart=-1,observerInitial=-1;
    static String pending;

    static void write(String file,String value) {
        try {Files.createDirectories(OUT);Files.writeString(OUT.resolve(file),value);}
        catch(Exception e){throw new RuntimeException(e);}
    }
    static void shot(String name) {if(SHOTS.add(name)){pending=ROLE+"-"+name+".png";ChaosticSpells.LOGGER.info("[WHIP-QA] {} screenshot {}",ROLE,name);}}
    static void input(byte action){PacketDistributor.sendToServer(new WhipActionPayload(action));}
    @EventBusSubscriber(modid=ChaosticSpells.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Boot {
        @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->initialize(Minecraft.getInstance()));}
    }
    static void initialize(Minecraft c) {
        if(initialized)return;
        initialized=true;GLFW.glfwHideWindow(c.getWindow().getWindow());
        c.options.pauseOnLostFocus=false;c.options.renderDistance().set(4);c.options.framerateLimit().set(60);c.options.fov().set(70);
        c.options.onboardAccessibility=false;c.options.skipMultiplayerWarning=true;c.options.save();
        write(ROLE+"-started.txt","Native client probe initialized; directory="+c.gameDirectory);
    }
    static void bootstrap(Minecraft c) {
        initialize(c);
        if(!opened && ROLE.equals("host") && c.getOverlay()==null && c.screen!=null && c.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen"))c.setScreen(new TitleScreen());
        if(ROLE.equals("host") && !opened && c.screen instanceof TitleScreen && c.getOverlay()==null) {
            opened=true;write("host-loading.txt","Creating or loading the isolated QA world");
            var flows=c.createWorldOpenFlows();
            if(new java.io.File(c.gameDirectory,"saves/"+WORLD+"/level.dat").exists())flows.openWorld(WORLD,()->{});
            else flows.createFreshLevel(WORLD,new LevelSettings("Whip visual QA",GameType.CREATIVE,false,Difficulty.PEACEFUL,!COMMANDS,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(0,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),c.screen);
        }
    }
    static void setupServer() {
        var c=Minecraft.getInstance();var server=c.getSingleplayerServer();
        server.execute(()->{
            var owner=server.getPlayerList().getPlayer(OwnersWhipItem.OWNER_UUID);
            if(owner==null){write("host-failure.txt","Configured owner UUID was not used by the integrated client");return;}
            var level=owner.serverLevel();
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
            level.setDayTime(6000);level.setWeatherParameters(100000,0,false,false);
            for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)level.setBlockAndUpdate(new BlockPos(x,64,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());
            owner.setGameMode(GameType.CREATIVE);owner.teleportTo(.5,65,.5);owner.setYRot(0);owner.yBodyRot=0;owner.setXRot(0);
            owner.getInventory().clearContent();owner.setItemSlot(EquipmentSlot.MAINHAND,ChaosticItemRegistry.OWNERS_WHIP.get().getDefaultInstance());
            owner.containerMenu.broadcastChanges();
            if(COMMANDS){server.getCommands().sendCommands(owner);return;}
            try {server.setUsesAuthentication(false);server.getConnection().startTcpServerListener(InetAddress.getByName("127.0.0.1"),25574);}
            catch(Exception e){write("host-failure.txt",e.toString());}
            ChaosticSpells.LOGGER.info("[WHIP-QA] Loopback-only test server is listening on 127.0.0.1:25574");
        });
    }
    @SubscribeEvent public static void join(PlayerEvent.PlayerLoggedInEvent e) {
        if(ROLE.equals("host") && e.getEntity() instanceof ServerPlayer p && p.getGameProfile().getName().equals("WhipObserver")) {
            p.setGameMode(GameType.SPECTATOR);p.teleportTo(4.5,65.2,.5);p.connection.teleport(4.5,65.2,.5,90,10);
            ChaosticSpells.LOGGER.info("[WHIP-QA] Real second client joined after the whip was opened");
        }
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        var c=Minecraft.getInstance();ticks++;
        bootstrap(c);
        c.setWindowActive(true);
        if(ticks%100==0)write(ROLE+"-status.txt","tick="+ticks+", worldTick="+worldTicks+", screen="+(c.screen==null?"null":c.screen.getClass().getSimpleName())+", overlay="+(c.getOverlay()==null?"null":c.getOverlay().getClass().getSimpleName()));
        if(ticks>12000){write(ROLE+"-failure.txt","Visual regression timeout; screen="+c.screen);c.stop();return;}
        if(c.level==null || c.player==null)return;
        worldTicks++;c.mouseHandler.releaseMouse();
        if(ROLE.equals("host") && !prepared && c.getSingleplayerServer()!=null){prepared=true;setupServer();}
        if(COMMANDS){checkCommands(c);return;}
        var owner=c.level.getPlayerByUUID(OwnersWhipItem.OWNER_UUID);
        var state=owner==null?null:WhipClientMotion.forPlayer(owner);
        if(state==null)return;
        var p=state.current;TRACE.put(p.tick(),Arrays.hashCode(p.points()));
        if(releaseAt==ticks){input(WhipActionPayload.RELEASE);releaseAt=-1;}
        if(ROLE.equals("host")) {
            c.options.setCameraType(CameraType.FIRST_PERSON);
            if(worldTicks<200){c.player.setYRot(0);c.player.setXRot(12);}
            if(worldTicks>=50 && preparation<0){shot("coiled-first-person");input(WhipActionPayload.START);releaseAt=ticks+1;preparation=0;}
            if(preparation==0 && p.kind()==WhipMotionPayload.ATTACK)preparation=p.started();
            if(worldTicks>=200 && p.loose() && !ready){ready=true;shot("loose-first-person");write("ready.txt","127.0.0.1:25574");}
            if(ready && !started && Files.exists(OUT.resolve("observer-ready.txt"))) {started=true;input(WhipActionPayload.START);releaseAt=ticks+1;}
            if(started && testStart<0 && p.kind()==WhipMotionPayload.ATTACK && p.started()>preparation)testStart=p.started();
        } else {
            c.options.setCameraType(CameraType.FIRST_PERSON);c.options.fov().set(55);c.player.setYRot(90);c.player.setXRot(10);
            if(worldTicks>=70 && !ready && p.loose()) {ready=true;observerInitial=p.started();shot("late-tracking-loose");write("observer-ready.txt","Received an already loose authoritative rope from the real server");}
            if(ready && testStart<0 && p.kind()==WhipMotionPayload.ATTACK && p.started()>observerInitial)testStart=p.started();
        }
        if(testStart>=0) {
            long age=c.level.getGameTime()-testStart;
            if(age>=9 && age<=12)shot("backswing");
            if(age>=15 && age<=18)shot("forward-cast");
            if(age>=30 && age<=33)shot("crack");
            if(age>=100)shot("settled");
            if(ROLE.equals("host") && age>=110){c.player.setYRot(95);c.player.setXRot(45);}
            if(age>=140)shot("look-turned-after-hit");
            if(age>=165 && !finished) {
                finished=true;StringBuilder json=new StringBuilder("{");
                for(var entry:TRACE.entrySet()){if(json.length()>1)json.append(',');json.append('"').append(entry.getKey()).append("\":").append(entry.getValue());}
                write(ROLE+"-trace.json",json.append('}').toString());write(ROLE+"-done.txt","Finished native client screenshots and world snapshot trace");
                ChaosticSpells.LOGGER.info("[WHIP-QA] {} completed {} screenshots",ROLE,SHOTS.size());
            }
        }
        if(finished && (!ROLE.equals("host") || Files.exists(OUT.resolve("observer-done.txt")))) {if(++doneTicks>30)c.stop();}
    }
    static void checkCommands(Minecraft c) {
        try {
            if(finished){if(++doneTicks>20)c.stop();return;}
            if(worldTicks>500)throw new IllegalStateException("Timed out waiting for command tree or responses");
            if(worldTicks<80 || c.player.connection.getCommands().getRoot().getChild("ave")==null)return;
            if(commandStarted<0) {
                if(c.player.hasPermissions(2))throw new IllegalStateException("QA owner unexpectedly has OP/cheats");
                var dispatcher=c.player.connection.getCommands();var source=c.player.connection.getSuggestionsProvider();
                for(String input:List.of("ave give @s minecraft:diamond 3","ave effect give @s minecraft:speed 10 1 true",
                        "ave damage @e[limit=1] 4 minecraft:generic","ave kill @e[limit=1]",
                        "ave locate biome minecraft:plains","ave tp @s ~1 ~2 ~3")) {
                    var parsed=dispatcher.parse(input,source);
                    if(parsed.getReader().canRead() || !parsed.getExceptions().isEmpty())throw new IllegalStateException("Client failed to parse "+input+": "+parsed.getExceptions());
                }
                boolean denied=false;
                try{net.minecraft.commands.arguments.EntityArgument.entities().parse(new com.mojang.brigadier.StringReader("@s"),source);}
                catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){denied=true;}
                if(!denied)throw new IllegalStateException("Unprefixed selector parsing unexpectedly gained permissions");
                commandSuggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("ave give @",source));
                c.player.connection.sendCommand("ave give @s minecraft:diamond 3");
                c.player.connection.sendCommand("ave effect give @s minecraft:speed 10 1 true");
                commandStarted=worldTicks;
            } else if(worldTicks-commandStarted>=60 && commandSuggestions.isDone()) {
                if(c.player.getInventory().countItem(net.minecraft.world.item.Items.DIAMOND)!=3)throw new IllegalStateException("Give failed over the real command connection");
                if(!c.player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED))throw new IllegalStateException("Effect failed over the real command connection");
                if(c.player.hasPermissions(2))throw new IllegalStateException("Alias permanently elevated the player");
                if(commandSuggestions.join().getList().stream().noneMatch(s->s.getText().equals("@s")))throw new IllegalStateException("Missing server-backed @s completion");
                write("commands-done.txt","Native owner client with cheats/OP disabled: all six aliases parse, selectors complete, give delivers 3 diamonds and effect applies through real command packets. Unprefixed selectors remain restricted; OP stays disabled.");
                ChaosticSpells.LOGGER.info("[AVE-COMMAND-QA] All native client and command packet checks passed");finished=true;
            }
        } catch(Exception failure){write("commands-failure.txt",failure.toString());ChaosticSpells.LOGGER.error("[AVE-COMMAND-QA] Failed",failure);c.stop();}
    }

    @SubscribeEvent public static void render(RenderFrameEvent.Post e) {
        var client=Minecraft.getInstance();bootstrap(client);
        if(++frames%100==0)write(ROLE+"-render.txt","frame="+frames+", screen="+(client.screen==null?"null":client.screen.getClass().getSimpleName())+", overlay="+(client.getOverlay()==null?"null":client.getOverlay().getClass().getSimpleName()));
        if(pending!=null) {
            var c=Minecraft.getInstance();String file=pending;pending=null;
            Screenshot.grab(c.gameDirectory,file,c.getMainRenderTarget(),message->ChaosticSpells.LOGGER.info("[WHIP-QA] {}",message.getString()));
        }
    }
}

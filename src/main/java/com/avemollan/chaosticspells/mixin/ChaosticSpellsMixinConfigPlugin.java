package com.avemollan.chaosticspells.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.LoadingModList;

/**
 * Skips Born in Chaos–targeting mixins when the mod jar is not on the classpath.
 */
public class ChaosticSpellsMixinConfigPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    private boolean bornInChaosPresent;
    private boolean viltrumitePairPresent;

    @Override
    public void onLoad(String mixinPackage) {
        // Do not load a mod's registry class while Mixin is still preparing transformations.
        this.bornInChaosPresent = LoadingModList.get().getModFileById("born_in_chaos_v1") != null;
        this.viltrumitePairPresent = LoadingModList.get().getModFileById("viltrumitecore") != null
                && LoadingModList.get().getModFileById("viltrumiteflight") != null;
        if (!bornInChaosPresent) {
            LOGGER.debug("ChaosticSpells mixin plugin: Born in Chaos not found; nightmare-set shim mixins will be skipped.");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("com.avemollan.chaosticspells.mixin.compat.viltrumite.")) return viltrumitePairPresent;
        if (!bornInChaosShimMixin(mixinClassName)) {
            return true;
        }
        return bornInChaosPresent;
    }

    /** Mixins injected into Born in Chaos generated classes via {@link org.spongepowered.asm.mixin.Mixin#targets()} */
    private static boolean bornInChaosShimMixin(String mixinClassName) {
        return mixinClassName.startsWith("com.avemollan.chaosticspells.mixin.compat.bicnightmare.")
                || mixinClassName.startsWith("com.avemollan.chaosticspells.mixin.compat.bicdarkmetal.");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // noop
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // noop
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith(".CorePlayerAccessMixin")) {
            guardMergedCallback(targetClass, "dev.baranhan.viltrumitecore.mixin.PlayerEntityCoreMixin", "onTick", 0, "needsCoreTick");
        } else if (mixinClassName.endsWith(".FlightPlayerAccessMixin")) {
            preserveCreativeFlying(targetClass);
        } else if (mixinClassName.endsWith(".ClientHandAccessMixin")) {
            guardMergedCallback(targetClass, "dev.baranhan.viltrumitecore.client.mixin.AlwaysVisibleOffhandMixin", "forceRenderEmptyOffhand", 1, "isActive");
        }
    }

    private static void preserveCreativeFlying(ClassNode target) {
        int count=0;
        for (MethodNode method : target.methods) {
            if (!method.name.equals("stopFlight") || !mergedFrom(method.visibleAnnotations,
                    "dev.baranhan.viltrumiteflight.mixin.PlayerEntityMixin")) continue;
            for (var instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode field && field.getOpcode()==Opcodes.PUTFIELD
                        && field.owner.equals("net/minecraft/world/entity/player/Abilities") && field.name.equals("flying") && field.desc.equals("Z")) {
                    method.instructions.insertBefore(field,new VarInsnNode(Opcodes.ALOAD,0));
                    method.instructions.set(field,new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "com/avemollan/chaosticspells/compat/viltrumite/AveViltrumiteAccess","stopVanillaFlying",
                            "(Lnet/minecraft/world/entity/player/Abilities;ZLnet/minecraft/world/entity/Entity;)V",false));
                    method.maxStack=Math.max(method.maxStack,3);
                    count++;
                }
            }
        }
        if (count!=1) throw new IllegalStateException("Unsupported ViltrumiteFlight stopFlight.flying writes: "+count);
        LOGGER.info("ChaosticSpells Ave policy: preserved creative/spectator flight in ViltrumiteFlight.stopFlight");
    }

    /** Guard the foreign callback rather than cancel vanilla tick/hand rendering. No external JAR is rewritten. */
    private static void guardMergedCallback(ClassNode target, String source, String originalName, int entityLocal, String policyMethod) {
        int count = 0;
        for (MethodNode method : target.methods) {
            if (!(method.name.equals(originalName) || method.name.endsWith("$" + originalName))) continue;
            if (!mergedFrom(method.visibleAnnotations, source) && !mergedFrom(method.invisibleAnnotations, source)) continue;
            InsnList guard = new InsnList();
            LabelNode allowed = new LabelNode();
            guard.add(new VarInsnNode(Opcodes.ALOAD, entityLocal));
            guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/avemollan/chaosticspells/compat/viltrumite/AveViltrumiteAccess", policyMethod,
                    "(Lnet/minecraft/world/entity/Entity;)Z", false));
            guard.add(new JumpInsnNode(Opcodes.IFNE, allowed));
            guard.add(new InsnNode(Opcodes.RETURN));
            guard.add(allowed);
            method.instructions.insert(guard);
            method.maxStack = Math.max(method.maxStack, 1);
            count++;
        }
        if (count != 1) throw new IllegalStateException("Unsupported ViltrumiteCore callback: " + source + "." + originalName + "; found " + count);
        LOGGER.info("ChaosticSpells Ave policy: guarded {}.{}", source, originalName);
    }

    private static boolean mergedFrom(List<AnnotationNode> annotations, String source) {
        if (annotations == null) return false;
        for (AnnotationNode annotation : annotations) {
            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") || annotation.values == null) continue;
            for (int i = 0; i < annotation.values.size(); i += 2) {
                if (annotation.values.get(i).equals("mixin") && annotation.values.get(i + 1).equals(source)) return true;
            }
        }
        return false;
    }
}

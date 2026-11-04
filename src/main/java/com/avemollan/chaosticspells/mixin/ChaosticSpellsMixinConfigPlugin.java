package com.avemollan.chaosticspells.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
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

    @Override
    public void onLoad(String mixinPackage) {
        // Do not load a mod's registry class while Mixin is still preparing transformations.
        this.bornInChaosPresent = LoadingModList.get().getModFileById("born_in_chaos_v1") != null;
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
        // noop
    }
}

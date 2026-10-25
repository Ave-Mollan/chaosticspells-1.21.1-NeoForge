package com.avemollan.chaosticspells.mixin.compat.bicdarkmetal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.LivingEntity;

/**
 * Born in Chaos uses {@literal f <= 6.0f} after {@code getHealth()} in
 * {@code DarkMetalArmorSobytiieTaktovShliemaProcedure}; this rebases {@code f} so that inequality matches
 * {@code health <= maxHealth * 30%}, same as legacy compat {@code MagicDarkMetalArmorItem}.
 *
 * <p>Algebra: inject {@code f = health * (6 / (maxHealth * RATIO))}; then {@code f <= 6 <=> health <= max * RATIO}.
 */
@Mixin(targets = "net.mcreator.borninchaosv.procedures.DarkMetalArmorSobytiieTaktovShliemaProcedure")
public class BcDarkMetalArmorPercentThresholdMixin {

    private static final float BIC_LITERAL_THRESHOLD = 6.0f;
    /** Mirrors {@code MagicDarkMetalArmorItem} / ExtendedArmorMaterials compat (≤30% HP). */
    private static final float HEALTH_FRACTION = 0.30f;

    @Redirect(
            method = "execute(Lnet/minecraft/world/entity/Entity;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;getHealth()F"))
    private static float chaosticspells$remapHealthForVanillaSixHeartGate(LivingEntity living) {
        float realHealth = living.getHealth();
        float ratioThreshold = Math.max(living.getMaxHealth() * HEALTH_FRACTION, 1e-4f);
        return realHealth * (BIC_LITERAL_THRESHOLD / ratioThreshold);
    }
}

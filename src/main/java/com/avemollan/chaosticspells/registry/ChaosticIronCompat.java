package com.avemollan.chaosticspells.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.resources.ResourceKey;

/**
 * Holder keys inside Irons Spellbooks datapack registries.
 */
public final class ChaosticIronCompat {
    public static final ResourceKey<UpgradeOrbType> CHAOS_SPELL_POWER_UPGRADE_ORB_TYPE =
            ResourceKey.create(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY, IronsSpellbooks.id("chaos_power"));

    private ChaosticIronCompat() {
    }
}

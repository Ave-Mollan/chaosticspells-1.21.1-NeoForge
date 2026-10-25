package com.avemollan.chaosticspells.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;

/**
 * Lets Born in Chaos MCreator procedures treat ChaosticSpells Magic Nightmare pieces as vanilla BiC
 * nightmare mantle items during {@code ItemStack#getItem()} identity checks.
 */
public final class BornInChaosNightmareArmorCompatBridge {

    private static final String BORN_IN_CHAOS = "born_in_chaos_v1";

    private static volatile boolean bicPiecesResolved;

    private static Item bicHelmet = Items.AIR;
    private static Item bicChest = Items.AIR;
    private static Item bicLeggings = Items.AIR;
    private static Item bicBoots = Items.AIR;

    private BornInChaosNightmareArmorCompatBridge() {
    }

    /**
     * @return BiC mantle item equivalent when {@code worn} is our magic piece; otherwise {@code worn}
     */
    public static Item unwrapForProcedure(Item worn) {
        if (worn == null || worn == Items.AIR) {
            return worn;
        }
        if (!ModList.get().isLoaded(BORN_IN_CHAOS)) {
            return worn;
        }
        resolveBic();

        Item h = ChaosticItemRegistry.MAGIC_NIGHTMARE_HELMET.get();
        Item c = ChaosticItemRegistry.MAGIC_NIGHTMARE_CHESTPLATE.get();
        Item l = ChaosticItemRegistry.MAGIC_NIGHTMARE_LEGGINGS.get();
        Item b = ChaosticItemRegistry.MAGIC_NIGHTMARE_BOOTS.get();

        if (worn == h && bicHelmet != Items.AIR) {
            return bicHelmet;
        }
        if (worn == c && bicChest != Items.AIR) {
            return bicChest;
        }
        if (worn == l && bicLeggings != Items.AIR) {
            return bicLeggings;
        }
        if (worn == b && bicBoots != Items.AIR) {
            return bicBoots;
        }
        return worn;
    }

    private static void resolveBic() {
        if (bicPiecesResolved) {
            return;
        }
        bicHelmet = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS, "nightmare_mantleofthe_night_helmet"));
        bicChest = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS, "nightmare_mantleofthe_night_chestplate"));
        bicLeggings = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS, "nightmare_mantleofthe_night_leggings"));
        bicBoots = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS, "nightmare_mantleofthe_night_boots"));
        bicPiecesResolved = true;
    }

    public static boolean isNightmarePiece(Item worn, EquipmentSlot slot) {
        resolveBic();
        Item expected = switch (slot) {
            case HEAD -> bicHelmet;
            case CHEST -> bicChest;
            case LEGS -> bicLeggings;
            case FEET -> bicBoots;
            default -> Items.AIR;
        };
        return expected != Items.AIR && unwrapForProcedure(worn) == expected;
    }
}

package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.commands.AveInventoryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChaosticMenuRegistry {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ChaosticSpells.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<AveInventoryMenu>> AVE_INVENTORY =
            MENUS.register("ave_inventory", () -> IMenuTypeExtension.create(AveInventoryMenu::client));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    private ChaosticMenuRegistry() {}
}

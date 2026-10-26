package com.avemollan.chaosticspells.compat.viltrumite;

import com.avemollan.chaosticspells.item.OwnersWhipItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

/** Does not link any optional mod classes. The server's saved opt-in is authoritative. */
public final class AveViltrumiteAccess {
    public static final String ENABLED_TAG = "chaosticspells:ave_viltrumite";

    /** ViltrumiteFlight invokes stopFlight even when already stopped. Preserve vanilla game modes. */
    public static void stopVanillaFlying(net.minecraft.world.entity.player.Abilities abilities, boolean flying, Entity entity) {
        if (entity instanceof net.minecraft.world.entity.player.Player player && (player.isCreative() || player.isSpectator())) return;
        abilities.flying = flying;
    }

    public static boolean available() {
        return ModList.get().isLoaded("viltrumitecore") && ModList.get().isLoaded("viltrumiteflight");
    }

    public static boolean isOwner(Entity entity) {
        return entity != null && OwnersWhipItem.OWNER_UUID.equals(entity.getUUID());
    }

    public static boolean selected(Entity entity) {
        return isOwner(entity) && entity.getPersistentData().getBoolean(ENABLED_TAG);
    }

    public static boolean mayHaveRace(Entity entity) {
        // On clients the existing synced entity data carries the server's decision.
        return isOwner(entity) && (entity.level().isClientSide() || selected(entity));
    }

    public static boolean isActive(Entity entity) {
        return isOwner(entity) && entity instanceof AveRaceState state && state.chaosticspells$isViltrumite();
    }

    public static boolean needsCoreTick(Entity entity) {
        return isActive(entity) || entity.getTags().contains("ViltrumiteGrabbed");
    }

    public static boolean isViltrumiteNamespace(String namespace) {
        return namespace.equals("viltrumitecore") || namespace.equals("viltrumiteflight");
    }

    public static boolean allowPayload(Entity sender, ResourceLocation type) {
        if (!isViltrumiteNamespace(type.getNamespace())) return true;
        // Race selection is exclusively /ave race, even for the owner and administrators.
        return !type.getPath().equals("race_choice") && isActive(sender);
    }

    public static boolean allowScreen(String className, boolean active) {
        if (className.equals("dev.baranhan.viltrumitecore.client.gui.RaceSelectionScreen")) return false;
        return active || !(className.startsWith("dev.baranhan.viltrumitecore.client.gui.")
                || className.startsWith("dev.baranhan.viltrumiteflight.client.gui."));
    }

    private AveViltrumiteAccess() {}
}

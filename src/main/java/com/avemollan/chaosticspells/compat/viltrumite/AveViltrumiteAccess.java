package com.avemollan.chaosticspells.compat.viltrumite;

import com.avemollan.chaosticspells.item.OwnersWhipItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

/** Does not link any optional mod classes. The server's saved opt-in is authoritative. */
public final class AveViltrumiteAccess {
    public static final String ENABLED_TAG = "chaosticspells:ave_viltrumite";

    public static final String GRABBED_TAG = "ViltrumiteGrabbed", RELEASE_TAG = "chaosticspells:ave_grab_release";

    /**
     * Replaces stopFlight's flying write and its unconditional onUpdateAbilities: ViltrumiteFlight calls it every tick
     * for every player. Vanilla modes and humans (whose custom flight is always NONE) keep any other flight source.
     */
    public static void stopVanillaFlying(net.minecraft.world.entity.player.Abilities abilities, boolean flying, Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || player.isCreative() || player.isSpectator()
                || abilities.flying == flying || !isActive(player) && !player.getTags().contains(GRABBED_TAG)) return;
        abilities.flying = flying;
        player.onUpdateAbilities();
    }

    public static boolean available() {
        return ModList.get().isLoaded("viltrumitecore") && ModList.get().isLoaded("viltrumiteflight");
    }

    public static boolean isOwner(Entity entity) {
        return OwnersWhipItem.isOwner(entity);
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

    /** Guards Core's per-tick callback. Also undoes the grab's mayfly when Core's failsafe drops the tag without releaseTarget. */
    public static boolean needsCoreTick(Entity entity) {
        if (isActive(entity)) return true;
        if (entity.getTags().contains(GRABBED_TAG)) {
            entity.addTag(RELEASE_TAG);
            return true;
        }
        if (entity.removeTag(RELEASE_TAG) && entity instanceof net.minecraft.world.entity.player.Player player
                && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        return false;
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

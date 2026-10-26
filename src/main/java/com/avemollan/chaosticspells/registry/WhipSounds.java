package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WhipSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ChaosticSpells.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> STRIKE_1 = register("whip_strike_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> STRIKE_2 = register("whip_strike_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> STRIKE_3 = register("whip_strike_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> STRIKE_4 = register("whip_strike_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOUBLE = register("whip_double");
    public static final DeferredHolder<SoundEvent, SoundEvent> FOLLOW_UP = register("whip_follow_up");
    public static final int FOLLOW_UP_PEAK = 8;

    public static SoundEvent single(int index) {
        return switch (index) {
            case 0 -> STRIKE_1.get(); case 1 -> STRIKE_2.get(); case 2 -> STRIKE_3.get(); default -> STRIKE_4.get();
        };
    }

    public static int peak(int index) { return switch (index) { case 0 -> 11; case 1 -> 10; case 2 -> 8; default -> 9; }; }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private WhipSounds() {}
}

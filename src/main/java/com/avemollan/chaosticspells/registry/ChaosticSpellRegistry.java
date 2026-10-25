package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.spells.ChaoticArrowSpell;
import com.avemollan.chaosticspells.spells.DeathPactSpell;
import com.avemollan.chaosticspells.spells.MagicArrowSpell;
import com.avemollan.chaosticspells.spells.ObsessionSpell;
import com.avemollan.chaosticspells.spells.PumpkinBarrageSpell;
import com.avemollan.chaosticspells.spells.RaiseBabySkeletonSpell;
import com.avemollan.chaosticspells.spells.RaiseFallenChaosKnightSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ChaosticSpellRegistry {
    private static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, ChaosticSpells.MODID);

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    private static Supplier<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static final Supplier<AbstractSpell> CHAOTIC_ARROW = registerSpell(new ChaoticArrowSpell());
    public static final Supplier<AbstractSpell> MAGIC_ARROW = registerSpell(new MagicArrowSpell());
    public static final Supplier<AbstractSpell> DEATH_PACT = registerSpell(new DeathPactSpell());
    public static final Supplier<AbstractSpell> OBSESSION = registerSpell(new ObsessionSpell());
    public static final Supplier<AbstractSpell> PUMPKIN_BARRAGE = registerSpell(new PumpkinBarrageSpell());
    public static final Supplier<AbstractSpell> RAISE_FALLEN_CHAOS_KNIGHT = registerSpell(new RaiseFallenChaosKnightSpell());
    public static final Supplier<AbstractSpell> RAISE_BABY_SKELETON = registerSpell(new RaiseBabySkeletonSpell());

    private ChaosticSpellRegistry() {
    }
}

package com.avemollan.chaosticspells.util;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Reads package-private {@link SchoolType#powerAttribute} — no public getter in Iron's API.
 */
public final class SchoolTypePowerAttributeAccess {
    private static final Field POWER_ATTRIBUTE_FIELD;

    static {
        try {
            POWER_ATTRIBUTE_FIELD = SchoolType.class.getDeclaredField("powerAttribute");
            POWER_ATTRIBUTE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Holder<Attribute> getPowerAttribute(SchoolType school) {
        try {
            return (Holder<Attribute>) POWER_ATTRIBUTE_FIELD.get(school);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable SchoolType findSchoolForPowerAttribute(Iterable<SchoolType> schools, Holder<Attribute> target) {
        for (SchoolType school : schools) {
            if (getPowerAttribute(school).equals(target)) {
                return school;
            }
        }
        return null;
    }

    private SchoolTypePowerAttributeAccess() {
    }
}

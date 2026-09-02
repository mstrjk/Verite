package teacommontea.util;

import java.lang.reflect.Field;


public final class NmsFields {

    private NmsFields() {}

    public static Field firstFieldOfAnyType(Class<?> start, String... typeNames) {
        for (Class<?> c = start; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                String tn = f.getType().getName();
                for (String want : typeNames) {
                    if (tn.equals(want)) return f;
                }
            }
        }
        return null;
    }

    public static Field firstFieldAssignableTo(Class<?> start, Class<?> type) {
        if (type == null) return null;
        for (Class<?> c = start; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (type.isAssignableFrom(f.getType())) return f;
            }
        }
        return null;
    }

    public static Field firstStaticFieldOfExactType(Class<?> owner, Class<?> type) {
        if (owner == null || type == null) return null;
        for (Field f : owner.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == type) {
                return f;
            }
        }
        return null;
    }

    public static Field firstFinalFieldProperSubtypeOf(Class<?> start, Class<?> base) {
        if (base == null) return null;
        for (Class<?> c = start; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                Class<?> t = f.getType();
                if (t != base && base.isAssignableFrom(t)
                        && java.lang.reflect.Modifier.isFinal(f.getModifiers())) {
                    return f;
                }
            }
        }
        return null;
    }
}


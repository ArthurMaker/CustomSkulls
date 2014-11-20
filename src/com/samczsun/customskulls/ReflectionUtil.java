package com.samczsun.customskulls;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtil {
    public static <T> T get(Object instance, String fieldName, Class<T> cast) throws IllegalArgumentException, IllegalAccessException {
        Field desiredField = getField(fieldName, instance.getClass(), true);
        if (desiredField != null) {
            desiredField.setAccessible(true);
            T result = cast.cast(desiredField.get(instance));
            desiredField.setAccessible(false);
            return result;
        } else {
            return null;
        }
    }

    public static void set(Object instance, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException {
        Field set = getField(fieldName, instance.getClass(), false);
        if (set != null) {
            set.setAccessible(true);
            set.set(instance, value);
            set.setAccessible(false);
        } else {
            throw new IllegalArgumentException(fieldName + " could not be found");
        }
    }

    public static void setFinal(Object instance, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException {
        Field set = getField(fieldName, instance.getClass(), false);
        if (set != null) {
            set.setAccessible(true);
            changeFinal(set, true);
            set.set(instance, value);
            set.setAccessible(false);
        }
    }

    public static void changeFinal(Field instance, boolean allow) throws IllegalArgumentException, IllegalAccessException {
        if (allow) {
            set(instance, "modifiers", instance.getModifiers() & ~Modifier.FINAL);
        } else {
            throw new UnsupportedOperationException("");
        }
    }

    public static Field getField(String name, Class<?> base, boolean deep) {
        Field targetField = null;
        Class<?> currentClass = base;
        if (deep) {
            while (!currentClass.equals(Object.class)) {
                try {
                    targetField = currentClass.getDeclaredField(name);
                } catch (NoSuchFieldException error) {
                }
                if (targetField != null) {
                    break;
                } else {
                    currentClass = currentClass.getSuperclass();
                }
            }
        } else {
            try {
                targetField = currentClass.getDeclaredField(name);
            } catch (NoSuchFieldException error) {

            }
        }
        return targetField;
    }
}

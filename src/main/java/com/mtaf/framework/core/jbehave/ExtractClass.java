package com.mtaf.framework.core.jbehave;

import java.lang.reflect.Field;

public class ExtractClass {
    private final String fieldName;

    private ExtractClass(String fieldName) {
        this.fieldName = fieldName;
    }

    public static ExtractClass field(String fieldName) {
        return new ExtractClass(fieldName);
    }

    public Object from(Object object) {
        try {
            Field field = object.getClass().getDeclaredField(this.fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException var3) {
            var3.printStackTrace();
        } catch (NoSuchFieldException var4) {
            var4.printStackTrace();
        }

        return null;
    }
}

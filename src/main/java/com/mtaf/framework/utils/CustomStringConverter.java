package com.mtaf.framework.utils;

import org.apache.commons.beanutils.Converter;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

public class CustomStringConverter implements Converter {

    private static final String NULL_VALUE = "<null>";

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object convert(final Class type, final Object value) {
        String converted = Optional.ofNullable(value).map(String::valueOf).orElse(null);
        if (isNull(converted) || NULL_VALUE.equals(converted)) {
            converted = null;
        }
        return converted;
    }

    public static String convertString(final String value) {
        return BeanUtilFactory.getBeanUtilsInstance().getConvertUtils().convert(value);
    }

    public static Map<String, String> convertValues(final Map<String, String> map) {
        return map.keySet().stream().collect(toMap(CustomStringConverter::convertString, key -> convertString(map.get(key))));
    }

}

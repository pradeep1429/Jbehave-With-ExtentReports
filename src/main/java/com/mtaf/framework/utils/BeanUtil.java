package com.mtaf.framework.utils;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mtaf.framework.utils.BeanUtilFactory.getBeanUtilsInstance;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class BeanUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertyReader.class);

    private BeanUtil() {
    }

    public static <T> List<T> populate(final ExamplesTable table, final Class<T> returnType) {
        return populate(table, returnType, true);
    }

    public static <T> List<T> populate(final ExamplesTable table, final Class<T> returnType,
    final boolean replaceNamedParameters) {
        return table.getRowsAsParameters(replaceNamedParameters).stream()
                .map(row -> populateNew(returnType, row.values()))
                .collect(toList());
    }

    public static <T> T populateNew(final Class<T> returnType, final Map<String, String> properties) {
        final T bean = BeanUtil.createNewInstance(returnType);
        populateProperties(bean, properties);
        return bean;
    }

    public static Map<String, String> populateToMap(final ExamplesTable table) {
        final String keyHeader = table.getHeaders().get(0);
        final String valueHeader = table.getHeaders().get(1);
        return table.getRowsAsParameters(true).stream()
                .map(Parameters::values)
                .collect(toMap(row -> row.get(keyHeader), row -> row.get(valueHeader)));
    }

    public static <T> T createNewInstance(final Class<T> type) {
        Objects.requireNonNull(type, "Cannot invoke method newInstance() from null Class.");
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(format("Failed to create instance of [%s]", type.getName()), e);
        }
    }

    public static void copyProperties(final Object destination, final Object source) {
        copyProperties(destination, source, format("Failed to copy properties from [%s] to [%s]",
                source.getClass().getSimpleName(), destination.getClass().getSimpleName()));
    }

    public static void copyProperties(final Object destination, final Object source, final String exceptionMessage) {
        try {
            getBeanUtilsInstance().copyProperties(destination, source);
        } catch (final IllegalAccessException | InvocationTargetException e1) {
            throw new IllegalStateException(exceptionMessage, e1);
        }
    }

    public static void populateProperties(final Object obj, final Map<String, String> properties) {
        Objects.requireNonNull(obj, "Cannot populate properties of null object.");
        try {
            getBeanUtilsInstance().populate(obj, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to populate properties of bean", e);
        }
    }

    public static List<String> getFirstColumnAsList(final ExamplesTable table, final boolean replaceNamedParameters) {
        if (table.getHeaders().size() > 1) {
            logger.debug("JBehave table has more that 1 column - first will be parsed to list, other ignored.");
        }
        final String header = table.getHeaders().get(0);
        final List<String> list = table.getRowsAsParameters(replaceNamedParameters).stream()
                .map(Parameters::values)
                .map(row -> row.get(header))
                .collect(toList());
        return list;
    }

    public static List<String> getFirstColumnAsList(final ExamplesTable table) {
        return getFirstColumnAsList(table, true);
    }

}

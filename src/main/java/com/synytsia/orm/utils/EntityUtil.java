package com.synytsia.orm.utils;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.synytsia.orm.annotation.Column;
import com.synytsia.orm.annotation.Entity;
import com.synytsia.orm.annotation.Id;
import com.synytsia.orm.annotation.Table;

import java.lang.reflect.Field;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;


public class EntityUtil {

    private static final Converter<String, String> UPPER_CAMEL_TO_LOWER_UNDERSCORE_CONVERTER = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
    private static final Converter<String, String> LOWER_CAMEL_TO_LOWER_UNDERSCORE_CONVERTER = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    private EntityUtil() {

    }

    public static Field findIdColumn(Class<?> entityType) {
        return stream(entityType.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(entityType.getName() + " entity doesn't have @Id"));
    }

    public static String resolveColumnName(Field field) {
        return ofNullable(field.getDeclaredAnnotation(Column.class))
                .map(Column::name)
                .orElseGet(() -> LOWER_CAMEL_TO_LOWER_UNDERSCORE_CONVERTER.convert(field.getName()));
    }

    public static String resolveTableName(Class<?> entityType) {
        return ofNullable(entityType.getDeclaredAnnotation(Table.class))
                .map(Table::name)
                .orElseGet(() -> UPPER_CAMEL_TO_LOWER_UNDERSCORE_CONVERTER.convert(entityType.getSimpleName()));
    }

    public static void verifyEntity(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException(entityType.getSimpleName() + " is not annotated with @Entity");
        }
    }
}

package com.synytsia.orm.utils;

import com.google.common.base.CaseFormat;
import com.synytsia.orm.annotation.Column;
import com.synytsia.orm.annotation.Entity;
import com.synytsia.orm.annotation.Id;
import com.synytsia.orm.annotation.Table;

import java.lang.reflect.Field;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;


public class EntityUtil {

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
                .orElseGet(() -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
    }

    public static String resolveTableName(Class<?> entityType) {
        return ofNullable(entityType.getDeclaredAnnotation(Table.class))
                .map(Table::name)
                .orElseGet(() -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityType.getSimpleName()));
    }

    public static void verifyEntity(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException(entityType.getSimpleName() + " is not annotated with @Entity");
        }
    }
}

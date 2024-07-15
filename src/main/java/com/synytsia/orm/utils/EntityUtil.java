package com.synytsia.orm.utils;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.synytsia.orm.annotation.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;


public class EntityUtil {

    private static final Converter<String, String> UPPER_CAMEL_TO_LOWER_UNDERSCORE_CONVERTER = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
    private static final Converter<String, String> LOWER_CAMEL_TO_LOWER_UNDERSCORE_CONVERTER = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    private EntityUtil() {

    }

    public static String resolveIdColumnName(Class<?> entityType) {
        return resolveColumnName(findIdColumn(entityType));
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

    public static Object[] toSnapshot(Object entity) {
        return stream(getUpdatableFieldsSortedByName(entity.getClass()))
                .map(f -> getFieldValueFromEntity(f, entity))
                .toArray();
    }

    public static Field[] getUpdatableFieldsSortedByName(final Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(EntityUtil::isFieldUpdatable)
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
    }

    public static boolean isFieldUpdatable(final Field field) {
        if (field.isAnnotationPresent(Id.class)) {
            return false;
        }
        // TODO relations between other entities
        return ofNullable(field.getAnnotation(Column.class))
                .map(Column::updatable)
                .orElse(true);
    }

    public static Object getFieldValueFromEntity(final Field field, final Object entity) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean areSnapshotsEqual(final Object[] current, final Object[] initial) {
        for (int i = 0; i < current.length; i++) {
            if (!Objects.equals(current[i], initial[i])) {
                return false;
            }
        }
        return true;
    }

    public static String resolveUpdateParams(final Class<?> entityType) {
        return Arrays.stream(getUpdatableFieldsSortedByName(entityType))
                .map(EntityUtil::resolveColumnName)
                .map(columnName -> columnName + " = ?")
                .collect(Collectors.joining(","));
    }

    public static boolean isEntity(Field field) {
        return field.isAnnotationPresent(ManyToOne.class);
    }

    public static boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    public static boolean isRegularColumn(Field field) {
        return !isEntity(field) && !isCollection(field);
    }

    public static String resolveJoinColumnName(Field field) {
        return ofNullable(field.getDeclaredAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown column name for relation '%s' in %s".formatted(field.getName(), field.getDeclaringClass().getName())));
    }
}

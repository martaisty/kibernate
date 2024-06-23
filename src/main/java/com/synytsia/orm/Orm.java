package com.synytsia.orm;

import com.google.common.base.CaseFormat;
import com.synytsia.orm.annotation.Column;
import com.synytsia.orm.annotation.Entity;
import com.synytsia.orm.annotation.Id;
import com.synytsia.orm.annotation.Table;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

public class Orm {

    private static final String SELECT_SQL = "SELECT * FROM %s WHERE %s = ?";

    private final DataSource dataSource;

    public Orm(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T findById(Class<T> entityType, Object id) {
        verifyEntity(entityType);
        final var tableName = resolveTableName(entityType);
        final var idColumn = findIdColumn(entityType);
        final var idColumnName = resolveColumnName(idColumn);
        final var selectSql = SELECT_SQL.formatted(tableName, idColumnName);

        try (final var c = dataSource.getConnection();
             final var statement = c.prepareStatement(selectSql)) {

            statement.setObject(1, id);
            final var rs = statement.executeQuery();

            if (rs.next()) {
                return createEntityFromResultSet(entityType, rs);
            }

            throw new RuntimeException("Entity not found (id=%s)".formatted(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyEntity(Class<?> entityType) {
        if(!entityType.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException(entityType.getSimpleName() + " is not annotated with @Entity");
        }
    }

    private <T> T createEntityFromResultSet(Class<T> entityType, ResultSet rs) {
        try {
            final var entity = entityType.getConstructor().newInstance();

            for (Field field : entityType.getDeclaredFields()) {
                final var columnName = resolveColumnName(field);
                final var fieldValue = rs.getObject(columnName);

                field.setAccessible(true);
                field.set(entity, fieldValue);
            }

            return entity;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Field findIdColumn(Class<?> entityType) {
        return stream(entityType.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(entityType.getName() + " entity doesn't have @Id"));
    }

    private String resolveColumnName(Field field) {
        return ofNullable(field.getDeclaredAnnotation(Column.class))
                .map(Column::name)
                .orElseGet(() -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
    }

    private String resolveTableName(Class<?> entityType) {
        return ofNullable(entityType.getDeclaredAnnotation(Table.class))
                .map(Table::name)
                .orElseGet(() -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityType.getSimpleName()));
    }
}

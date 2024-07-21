package com.synytsia.orm.session;

import com.synytsia.orm.collection.LazyList;
import com.synytsia.orm.utils.EntityUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.synytsia.orm.utils.EntityUtil.*;

public class SessionImpl implements Session {

    private static final String SELECT_BY_COLUMN_SQL = "SELECT * FROM %s WHERE %s = ?";
    private static final String UPDATE_BY_COLUMN_SQL = "UPDATE %s SET %s WHERE %s = ?";

    private final DataSource dataSource;
    private final Map<EntityKey, Object> entities = new HashMap<>();
    private final Map<EntityKey, Object[]> entitiesInitialSnapshot = new HashMap<>();

    private boolean isClosed = false;

    public SessionImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T findById(Class<T> entityType, Object id) {
        throwIfClosed();
        EntityUtil.verifyEntity(entityType);
        final var entityKey = new EntityKey(entityType, id);


        if (entities.containsKey(entityKey)) {
            System.out.printf("Returning entity from cache %s%n", entityKey);
            return entityType.cast(entities.get(entityKey));
        }

        final var tableName = resolveTableName(entityType);
        final var idColumnName = resolveIdColumnName(entityType);
        final var selectSql = SELECT_BY_COLUMN_SQL.formatted(tableName, idColumnName);

        try (final var c = dataSource.getConnection();
             final var statement = c.prepareStatement(selectSql)) {

            statement.setObject(1, id);
            System.out.println("SQL: " + selectSql);
            try (final var rs = statement.executeQuery()) {
                if (rs.next()) {
                    final var entity = this.<T>createEntityFromResultSet(entityKey, rs);
                    entities.put(entityKey, entity);
                    entitiesInitialSnapshot.put(entityKey, toSnapshot(entity));
                    return entity;
                }
            }

            throw new RuntimeException("Entity not found (id=%s)".formatted(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        try {
            dirtyCheck();
        } finally {
            entities.clear();
            entitiesInitialSnapshot.clear();
            isClosed = true;
        }
    }

    private <T> T createEntityFromResultSet(EntityKey entityKey, ResultSet rs) {
        try {
            final var entityType = (Class<T>) entityKey.type();
            final var entity = entityType.getConstructor().newInstance();

            for (Field field : entityType.getDeclaredFields()) {
                final Object fieldValue;

                if (isRegularColumn(field)) {
                    final var columnName = EntityUtil.resolveColumnName(field);

                    fieldValue = rs.getObject(columnName);
                } else if (isEntity(field)) {
                    final var joinColumnName = EntityUtil.resolveJoinColumnName(field);
                    final var relatedEntityId = rs.getObject(joinColumnName);

                    fieldValue = findById(field.getType(), relatedEntityId);
                } else if (isCollection(field)) {
                    final var collectionType = (ParameterizedType) field.getGenericType();
                    final var relatedEntityType = (Class<?>) collectionType.getActualTypeArguments()[0];
                    final var relatedEntityField = relatedEntityType.getDeclaredField(resolveOneToManyMappedBy(field));
                    final var relatedEntityColumnName = resolveJoinColumnName(relatedEntityField);
// TODO implement other collections and ManyToMany
                    fieldValue = new LazyList<>(() -> loadAllByColumn(relatedEntityType, relatedEntityColumnName, entityKey.id()));
                } else {
                    throw new IllegalArgumentException("Unsupported type(%s) in %s".formatted(field.getType().getName(), entityType.getName()));
                }

                field.setAccessible(true);
                field.set(entity, fieldValue);
            }

            return entity;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 SQLException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private List<?> loadAllByColumn(Class<?> entityType, String columnName, Object columnValue) {
        throwIfClosed();
        verifyEntity(entityType);

        final var tableName = resolveTableName(entityType);
        final var idColumnName = resolveIdColumnName(entityType);
        final var selectSql = SELECT_BY_COLUMN_SQL.formatted(tableName, columnName);

        System.out.println("SQL: " + selectSql);

        try (final var c = dataSource.getConnection();
             final var statement = c.prepareStatement(selectSql)) {
            statement.setObject(1, columnValue);
            final var list = new ArrayList<>();

            try (final var rs = statement.executeQuery()) {
                while (rs.next()) {
                    final var entityKey = new EntityKey(entityType, rs.getObject(idColumnName));
                    final var entity = createEntityFromResultSet(entityKey, rs);
                    entities.put(entityKey, entity);
                    entitiesInitialSnapshot.put(entityKey, toSnapshot(entity));
                    list.add(entity);
                }
            }

            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void dirtyCheck() {
        for (var entityEntry : entities.entrySet()) {
            final var key = entityEntry.getKey();
            final var entity = entityEntry.getValue();
            final var initialSnapshot = entitiesInitialSnapshot.get(key);
            final var currentSnapshot = toSnapshot(entity);

            if (!areSnapshotsEqual(currentSnapshot, initialSnapshot)) {
                updateDirtyEntityInDb(key, currentSnapshot);
            }
        }
    }

    private void updateDirtyEntityInDb(final EntityKey entityKey, final Object[] currentSnapshot) {
        final var tableName = resolveTableName(entityKey.type());
        final var idColumnName = resolveIdColumnName(entityKey.type());
        final var updateParams = resolveUpdateParams(entityKey.type());
        final var updateSql = UPDATE_BY_COLUMN_SQL.formatted(tableName, updateParams, idColumnName);
        System.out.println("SQL: " + updateSql);

        try (final var connection = dataSource.getConnection();
             final var updateStatement = connection.prepareStatement(updateSql)) {

            for (int i = 0; i < currentSnapshot.length; i++) {
                updateStatement.setObject(i + 1, currentSnapshot[i]);
            }
            updateStatement.setObject(currentSnapshot.length + 1, entityKey.id());
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.printf("Failed to update entity %s (id=%s)%n", entityKey.type().getName(), entityKey.id());
        }
    }

    private void throwIfClosed() {
        if (isClosed) {
            throw new IllegalArgumentException("Session is closed");
        }
    }
}

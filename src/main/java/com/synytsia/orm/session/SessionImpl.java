package com.synytsia.orm.session;

import com.synytsia.orm.utils.EntityUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SessionImpl implements Session {

    private static final String SELECT_BY_COLUMN_SQL = "SELECT * FROM %s WHERE %s = ?";

    private final DataSource dataSource;
    private final Map<EntityKey, Object> entities = new HashMap<>();

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
            System.out.println("Returning entity from cache");
            return entityType.cast(entities.get(entityKey));
        }

        final var tableName = EntityUtil.resolveTableName(entityType);
        final var idColumn = EntityUtil.findIdColumn(entityType);
        final var idColumnName = EntityUtil.resolveColumnName(idColumn);
        final var selectSql = SELECT_BY_COLUMN_SQL.formatted(tableName, idColumnName);

        try (final var c = dataSource.getConnection();
             final var statement = c.prepareStatement(selectSql)) {

            statement.setObject(1, id);
            final var rs = statement.executeQuery();

            if (rs.next()) {
                final var entity = createEntityFromResultSet(entityType, rs);
                entities.put(entityKey, entity);
                return entity;
            }

            throw new RuntimeException("Entity not found (id=%s)".formatted(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        // TODO dirty checking
        isClosed = true;
    }

    private <T> T createEntityFromResultSet(Class<T> entityType, ResultSet rs) {
        try {
            final var entity = entityType.getConstructor().newInstance();

            for (Field field : entityType.getDeclaredFields()) {
                final var columnName = EntityUtil.resolveColumnName(field);
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

    private void throwIfClosed() {
        if (isClosed) {
            throw new IllegalArgumentException("Session is closed");
        }
    }
}

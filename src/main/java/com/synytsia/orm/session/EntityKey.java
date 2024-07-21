package com.synytsia.orm.session;

import com.synytsia.orm.utils.EntityUtil;

import static java.util.Objects.requireNonNull;


public record EntityKey(Class<?> type, Object id) {
    public EntityKey {
        requireNonNull(type);
        requireNonNull(id);

        final var idType = EntityUtil.findIdColumn(type).getType();
        if (!idType.equals(id.getClass())) {
            // TODO think of something more convenient to support Long id with passing 1 instead of 1L
            throw new IllegalArgumentException("Entity %s has %s as an id type but provided %s"
                    .formatted(type.getName(), idType.getName(), id.getClass().getName()));
        }
    }
}

package com.synytsia.orm;

public interface Session {

    <T> T findById(Class<T> entityType, Object id);
}

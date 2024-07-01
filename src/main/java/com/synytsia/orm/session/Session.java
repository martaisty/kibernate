package com.synytsia.orm.session;

import java.io.Closeable;

public interface Session extends Closeable {

    <T> T findById(Class<T> entityType, Object id);
}

package com.synytsia.orm.impl;

import com.synytsia.orm.Session;
import com.synytsia.orm.SessionFactory;

import javax.sql.DataSource;

public class SessionFactoryImpl implements SessionFactory {

    private final DataSource dataSource;

    public SessionFactoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Session openSession() {
        return new SessionImpl(dataSource);
    }
}

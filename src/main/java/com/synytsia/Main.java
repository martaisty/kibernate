package com.synytsia;

import com.synytsia.demo.entity.User;
import com.synytsia.orm.Orm;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Main {
    public static void main(String[] args) {
        final var orm = new Orm(createDataSource());
        final var user = orm.findById(User.class, 3);
        System.out.println("Found user: " + user);
    }

    private static DataSource createDataSource() {
        final var postgres = new PGSimpleDataSource();
        postgres.setUrl("jdbc:postgresql://localhost:5432/postgres");
        postgres.setUser("postgres");
        postgres.setPassword("postgres");
        return postgres;
    }
}
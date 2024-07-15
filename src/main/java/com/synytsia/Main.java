package com.synytsia;

import com.synytsia.demo.entity.Skill;
import com.synytsia.demo.entity.User;
import com.synytsia.orm.session.SessionFactoryImpl;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var sessionFactory = new SessionFactoryImpl(createDataSource());
        try (final var session = sessionFactory.openSession()) {
            final var skill = session.findById(Skill.class, 1);
            final var user = session.findById(User.class, 1);

            System.out.println(skill);
            System.out.println(user);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DataSource createDataSource() {
        final var postgres = new PGSimpleDataSource();
        postgres.setUrl("jdbc:postgresql://localhost:5432/postgres");
        postgres.setUser("postgres");
        postgres.setPassword("postgres");
        return postgres;
    }
}
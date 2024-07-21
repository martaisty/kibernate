package com.synytsia;

import com.synytsia.demo.entity.Skill;
import com.synytsia.orm.session.SessionFactoryImpl;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var sessionFactory = new SessionFactoryImpl(createDataSource());
        try (final var session = sessionFactory.openSession()) {
            final var skill = session.findById(Skill.class, 1L);
            System.out.println(skill.getName());
            final var user = skill.getUser();
            System.out.println(user.getName());
            System.out.println(user.getSkills());

            user.getSkills().get(0).setName("Jogging");

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
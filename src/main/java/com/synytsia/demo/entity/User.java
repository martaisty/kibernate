package com.synytsia.demo.entity;

import com.synytsia.orm.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    private Integer age;

    @OneToMany(mappedBy = "user")
    @EqualsAndHashCode.Exclude
    private List<Skill> skills;
}

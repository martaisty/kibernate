package com.synytsia.demo.entity;

import com.synytsia.orm.annotation.Column;
import com.synytsia.orm.annotation.Entity;
import com.synytsia.orm.annotation.Id;
import com.synytsia.orm.annotation.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    private Integer age;
}

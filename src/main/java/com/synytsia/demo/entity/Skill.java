package com.synytsia.demo.entity;

import com.synytsia.orm.annotation.*;
import lombok.Data;

@Data
@Entity
@Table(name = "skills")
public class Skill {

    @Id
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

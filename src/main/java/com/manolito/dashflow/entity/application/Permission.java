package com.manolito.dashflow.entity.application;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "permissions", schema = "dashflow_appl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer id;

    @Column(name = "permission_name", nullable = false, unique = true)
    private String permissionName;

    @Column(name = "description")
    private String description;
}

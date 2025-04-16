package com.manolito.dashflow.entity.application;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "tools", schema = "dashflow_appl")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApplicationTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tool_id", nullable = false)
    private Integer id;

    @Column(name = "tool_name", nullable = false, unique = true)
    private String toolName;
}
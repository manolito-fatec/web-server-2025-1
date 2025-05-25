package com.manolito.dashflow.entity.application;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "accounts", schema = "dashflow_appl")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser applicationUser;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private ApplicationTool tool;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role roleId;

    @Column(name = "account")
    private String accountIdTool;

    @Column(name = "project")
    private String projectIdTool;

    public Account(ApplicationUser applicationUser, ApplicationTool applicationTool, Role roleId, String accountIdToll, String projectIdTool )
    {
        this.applicationUser = applicationUser;
        this.tool = applicationTool;
        this.roleId = roleId;
        this.accountIdTool = accountIdToll;
        this.projectIdTool = projectIdTool;
    }
}


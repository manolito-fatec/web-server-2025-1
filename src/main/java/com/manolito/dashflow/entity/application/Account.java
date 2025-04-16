package com.manolito.dashflow.entity.application;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "accounts", schema = "dashflow_appl")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Account {

    @EmbeddedId
    private AccountId id;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser applicationUser;

    @MapsId("toolId")
    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private ApplicationTool tool;

    @Column(name = "account")
    private String accountId;
}


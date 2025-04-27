package com.manolito.dashflow.entity.application;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AccountId implements Serializable {
    private Integer userId;
    private Integer toolId;
}

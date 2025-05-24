package com.manolito.dashflow.dto.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableAdminDto
{
    private String project;
    private String manager;
    private Integer quantityOfOperators;
}

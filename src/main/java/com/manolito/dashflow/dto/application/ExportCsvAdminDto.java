package com.manolito.dashflow.dto.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ExportCsvAdminDto{
     private String project;
     private String manager;
     private Integer quantityOfOperators;
     private Integer quantityOfCards;

}

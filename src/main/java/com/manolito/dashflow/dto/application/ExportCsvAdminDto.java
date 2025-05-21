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
public class ExportCsvAdminDto {
     private String Project;
     private String Manager;
     private Integer quantityOfOperators;
     private Integer quantityOfCards;

}

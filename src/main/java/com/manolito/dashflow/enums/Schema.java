package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Schema {
    DATAWAREHOUSE("dw_dashflow"),
    APPLICATION("dashflow_appl");

    private final String schema;
}

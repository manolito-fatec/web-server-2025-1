package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Schema {
    DATAWAREHOUSE("DW_DASHFLOW"),
    APPLICATION("DASHFLOW_APPL");

    private final String schema;
}

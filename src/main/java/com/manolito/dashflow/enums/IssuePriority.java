package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IssuePriority {
    HIGH("High"),
    NORMAL("Normal"),
    LOW("Low");

    private final String value;
}

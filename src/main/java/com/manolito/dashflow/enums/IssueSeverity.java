package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IssueSeverity {
    CRITICAL("Critical"),
    IMPORTANT("Important"),
    NORMAL("Normal"),
    MINOR("Minor"),
    WISHLIST("Wishlist"),;

    private final String value;

}

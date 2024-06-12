package com.aurora.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobStatusEnum {

    NORMAL(0),

    PAUSE(1);

    private final Integer value;

}


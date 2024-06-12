package com.aurora.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginTypeEnum {

    EMAIL(1, "邮箱登录", ""),

    THIRD(2, "第三方登录", "thirdLoginStrategyImpl");

    private final Integer type;

    private final String desc;

    private final String strategy;

}

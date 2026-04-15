package com.xiw.kuwei.constant;

import lombok.Getter;

@Getter
public enum CommonEnum {

    TRUE("t");

    private final String code;

    private final String desc;

    CommonEnum(String code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    CommonEnum(String code) {
        this.code = code;
        this.desc = null;
    }

}

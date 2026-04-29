package com.xiw.kuwei.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum SinaStockTypeEnum {

    // A股市场
    SH(List.of(1, 2, 9), "沪指"),
    SZ(List.of(11, 12, 13), "深指"),
    CYB(List.of(80), "创业板"),
    SME(List.of(64), "中小板"),

    // 港股 & 美股
    HK(List.of(31, 32, 33), "港股"),
    US(List.of(41), "美股"),
    UK(List.of(103), "英股"),

    // 指数
    INDEX(List.of(0, 10, 100), "指数"),
    FOREX(List.of(0, 10, 100), "外汇"),

    // 三板
    THIRD_MARKET(List.of(73), "三板"),
    BOND(List.of(81), "债券"),
    FUTURE(List.of(85, 86, 88), "期货"),

    // 板块（重点）
    INDUSTRY(List.of(90), "行业板块"),
    CONCEPT(List.of(91), "概念板块"),
    REGION(List.of(92), "地域板块"),
    SECTOR(List.of(102), "综合板块"),

    // 基金
    FUND(List.of(21, 22, 23, 24, 25, 26, 203), "基金"),

    UNKNOWN(Collections.emptyList(), "未知");

    private static final Map<Integer, SinaStockTypeEnum> TYPE_MAP = new HashMap<>();

    static {
        for (SinaStockTypeEnum value : values()) {
            for (Integer type : value.typeList) {
                TYPE_MAP.put(type, value);
            }
        }
    }

    private final List<Integer> typeList;

    private final String desc;

    SinaStockTypeEnum(List<Integer> typeList, String desc) {
        this.typeList = typeList;
        this.desc = desc;
    }

    /**
     * 获取中文描述（推荐用这个）
     */
    public static String getDescByType(int type) {
        SinaStockTypeEnum e = TYPE_MAP.get(type);
        return e == null ? UNKNOWN.desc : e.desc;
    }

    /**
     * 获取枚举（便于扩展）
     */
    public static SinaStockTypeEnum getByType(int type) {
        return TYPE_MAP.getOrDefault(type, UNKNOWN);
    }

    public String getDesc() {
        return desc;
    }

    public List<Integer> getTypeList() {
        return typeList;
    }
}
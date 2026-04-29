package com.xiw.kuwei.vo.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleStockVO {

    private String name;

    private String code;

    private int type;

    private String typeStr;

}

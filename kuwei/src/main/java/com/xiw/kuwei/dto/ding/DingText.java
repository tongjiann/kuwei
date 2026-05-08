package com.xiw.kuwei.dto.ding;

import lombok.Data;

/**
 * @author xiwang
 * @since 2022-04-22 18:09
 */
@Data
public class DingText {
    private String content;

    public DingText(String content) {
        this.content = content;
    }
}

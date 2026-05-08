package com.xiw.kuwei.dto.ding;

import lombok.Data;

/**
 * @author xiwang
 * @since 2022-02-28 00:00
 */
@Data
public class DingMessage {

    private String msgtype;

    private DingAt at;

    private DingMarkdown markdown;

    private DingText text;

    public DingMessage() {
    }

    public DingMessage(String msgtype) {
        this.msgtype = msgtype;
    }
}

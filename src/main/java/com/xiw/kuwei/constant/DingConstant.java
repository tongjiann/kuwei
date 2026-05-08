package com.xiw.kuwei.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author xiwang
 * @since 2022-02-28 00:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DingConstant {

    public static final String SEND_MSG_API = "https://oapi.dingtalk.com/robot/send";

    public static final String HMAC_SHA_256_KEY = "HmacSHA256";

    public static final String ACCESS_TOKEN_KEY = "access_token";

    public static final String TIMESTAMP_KEY = "timestamp";

    public static final String SIGN_KEY = "sign";

    public static final String MOBILE_PHONE = "18969257660";

    public static final String DEFAULT_MESSAGE_TITLE = "默认消息标题";

    public static final String XIW_EXCEPTION_INFO_PUSH = "Xiw异常信息推送";

    public static final String OK_KEY = "ok";

    public static final String TYPE_MARKDOWN = "markdown";

    public static final String TYPE_TEXT = "text";

    public static final String MESSAGE_ENABLE_KEY = "message.enable";

    public static final String MESSAGE_DING_DEFAULT_ACCESS_TOKEN_KEY = "message.ding.default.accessToken";

    public static final String MESSAGE_DING_DEFAULT_SECRET_KEY = "message.ding.default.secret";

    public static final String MESSAGE_DING_MSG_ACCESS_TOKEN_KEY = "message.ding.msg.accessToken";

    public static final String MESSAGE_DING_MSG_SECRET_KEY = "message.ding.msg.secret";

}

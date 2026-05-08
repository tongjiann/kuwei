package com.xiw.kuwei.factory;


import com.diboot.core.util.ContextHolder;
import com.xiw.kuwei.config.MessageConfig;
import com.xiw.kuwei.constant.MessageChannelConstant;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.helper.message.DingMessageHelper;
import com.xiw.kuwei.helper.message.MessageHelper;

/**
 * @author xiwang
 * @apiNote 返回发送消息的工具类
 * @since 2022-06-07 09:57
 */
public class MessageFactory {

    private MessageFactory() {
    }


    public static MessageHelper getMessageHelper() {
        MessageConfig messageConfig = ContextHolder.getBean(MessageConfig.class);
        Integer messageChannel = messageConfig.getChannel();
        if (messageChannel == MessageChannelConstant.MESSAGE_CHANNEL_DING) {
            return DingMessageHelperHolder.instance;
        }
        throw new LogicalException("未知的消息通道:" + messageChannel);
    }

    private static class DingMessageHelperHolder {
        private static final DingMessageHelper instance = new DingMessageHelper();
    }
}

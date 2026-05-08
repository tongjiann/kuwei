package com.xiw.kuwei.helper.message;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.xiw.kuwei.config.DingMessageConfig;
import com.xiw.kuwei.config.MessageConfig;
import com.xiw.kuwei.constant.DingConstant;
import com.xiw.kuwei.dto.ding.DingAt;
import com.xiw.kuwei.dto.ding.DingMarkdown;
import com.xiw.kuwei.dto.ding.DingMessage;
import com.xiw.kuwei.dto.ding.DingText;
import com.xiw.kuwei.exception.LogicalException;
import com.xiw.kuwei.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.xiw.kuwei.constant.DingConstant.*;
import static com.xiw.kuwei.constant.SymbolConstant.*;


/**
 * @author xiwang
 * @since 2022-02-28 00:00
 */
@Component
@Slf4j
public class DingMessageHelper implements MessageHelper {


    public static final String NOT_ENABLED_MESSAGE_PUSH = "未启用消息推送或消息未配送，暂不推送钉钉信息";

    /**
     * 用于加签的密钥
     */
    private static String secret;

    /**
     * 授权码
     */
    private static String accessToken;

    private static String msgSecret;

    private static String msgAccessToken;

    private static boolean isEnabled = true;

    static {
        isEnabled = MessageConfig.isEnable();
        if (isEnabled) {
            accessToken = DingMessageConfig.getDefaultAccessToken();
            secret = DingMessageConfig.getDefaultSecretKey();
            if (accessToken == null || secret == null) {
                isEnabled = false;
            }
            msgAccessToken = DingMessageConfig.getMsgAccessToken();
            msgSecret = DingMessageConfig.getMsgSecretKey();
            if (msgAccessToken == null || msgSecret == null) {
                msgAccessToken = accessToken;
                msgSecret = secret;
            }
        }
    }

    private static String getDingSendUrl() {
        return getDingSendUrl(accessToken, secret);
    }

    private static String getDingSendUrl(String accessToken, String secret) {
        String url;
        long timestamp = System.currentTimeMillis();
        var stringToSign = timestamp + "\n" + secret;
        String sign = null;
        try {
            var mac = Mac.getInstance(HMAC_SHA_256_KEY);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256_KEY));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            sign = URLEncoder.encode(Base64Encoder.encode(signData), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error(e.getMessage(), e);
        }
        if (sign == null) {
            throw new LogicalException("计算签名失败");
        }
        url = SEND_MSG_API + QUESTION_MARK_STRING + AND_STRING + ACCESS_TOKEN_KEY + EQUAL_STRING + accessToken +
                AND_STRING + TIMESTAMP_KEY + EQUAL_STRING + timestamp + AND_STRING + SIGN_KEY + EQUAL_STRING + sign;
        return url;

    }


    public static void main(String[] args) {
        send("markdown", "title",
                "this is a message sent to ding from kuwei");
    }

    public static void send(String msgType, String title, String text) {
        var dingMessage = new DingMessage();
        dingMessage.setMsgtype(msgType);
        var dingMarkdown = new DingMarkdown();
        dingMarkdown.setText(text);
        dingMarkdown.setTitle(title);
        dingMessage.setMarkdown(dingMarkdown);
        String url = getDingSendUrl("7189a9ad6680f2948c739b140a39322662f95107c0cf2bf29829b634af2b4e3c",
                "SECbe63136381474504f4a11a9c97d3469b2c1a11a691d7c40488a3a8736b3b3580");
        var jsonStr = JSONUtil
                .parse(dingMessage)
                .toString();
        log.info("向钉钉发送消息:{}", jsonStr);
        HttpUtil.post(url, jsonStr);
    }

    private static String doSend(String jsonStr, String url) {
        log.info("向钉钉发送消息:{}", jsonStr);
        return HttpUtil.post(url, jsonStr);
    }

    @Override
    public String sendMessage(String msg) {
        return sendMessage(msg, msgAccessToken, msgSecret);
    }

    private String sendMessage(String msg, String accessToken, String secret) {
        if (!isEnabled) {
            log.info(NOT_ENABLED_MESSAGE_PUSH);
            return EMPTY_STRING;
        }
        var dingMessage = new DingMessage(TYPE_TEXT);
        dingMessage.setText(new DingText(msg));
        String url = getDingSendUrl(accessToken, secret);
        var jsonStr = JSONUtil
                .parse(dingMessage)
                .toString();
        return doSend(jsonStr, url);
    }

    private String sendMarkDown(DingMessage dingMessage, String accessToken, String secret) {
        if (!isEnabled) {
            log.info(NOT_ENABLED_MESSAGE_PUSH);
            return EMPTY_STRING;
        }

        String url = getDingSendUrl(accessToken, secret);
        var jsonStr = JSONUtil
                .parse(dingMessage)
                .toString();
        return doSend(jsonStr, url);
    }


    public String createMessageTaskString(MessageTask messageTask) {
        if (messageTask == null) {
            return null;
        }
        return "**主题：**" + messageTask.getTopic() + "\n\n**提醒时间：**" + messageTask.getNextExecutionTime() +
                "\n\n**详情：**" + messageTask.getDetail() + "\n\n**重要程度：**" +
                buildStar(messageTask.getImportanceLevel());
    }


    @Override
    public String sendMessage(MessageTask messageTask) {
        if (!isEnabled) {
            log.info("未启用消息推送，暂不推送钉钉信息");
            return EMPTY_STRING;
        }
        var dingMessage = new DingMessage();
        dingMessage.setMsgtype(TYPE_MARKDOWN);
        long importance = messageTask.getImportanceLevel();
        if (importance > 3) {
            var dingAt = new DingAt();
            dingAt.setAtAll(true);
            dingMessage.setAt(dingAt);
            dingAt.setAtMobiles(Collections.singletonList(MOBILE_PHONE));
        }
        var dingMarkdown = new DingMarkdown();
        dingMarkdown.setText(createMessageTaskString(messageTask));
        dingMarkdown.setTitle(messageTask.getTopic());
        dingMessage.setMarkdown(dingMarkdown);
        return sendMarkDown(dingMessage);
    }


    private String sendMarkDown(DingMessage dingMessage) {
        return sendMarkDown(dingMessage, accessToken, secret);
    }


    @Override
    public <T, M extends T> String sendMessage(T t, Class<M> clazz) {
        return sendMessage(t, clazz, DEFAULT_MESSAGE_TITLE);
    }

    @Override
    public <T, M extends T> String sendMessage(T t, Class<M> clazz, String title) {
        Assert.notNull(t);
        return sendMessage(Collections.singletonList(t), clazz, title);
    }

    @Override
    public <T, M extends T> String sendMessage(Collection<T> collection, Class<M> clazz) {
        return sendMessage(collection, clazz, DingConstant.DEFAULT_MESSAGE_TITLE);
    }

    @Override
    public <T, M extends T> String sendMessage(Collection<T> collection, Class<M> clazz, String title) {
        if (!isEnabled) {
            log.info(NOT_ENABLED_MESSAGE_PUSH);
            return "";
        }
        Assert.notEmpty(collection);
        Assert.notNull(clazz);

        List<M> fundMarkDownList = BeanUtil.copyToList(collection, clazz);
        var dingMessage = new DingMessage();
        dingMessage.setMsgtype(TYPE_MARKDOWN);
        var dingAt = new DingAt();
        dingAt.setAtAll(true);
        dingAt.setAtMobiles(Collections.singletonList(MOBILE_PHONE));
        var dingMarkdown = new DingMarkdown();
        List<String> wallpaperMarkdownStrList = fundMarkDownList
                .stream()
                .map(Object::toString)
                .toList();
        var join = StringUtils.join(wallpaperMarkdownStrList, "\n--- \n");
        dingMarkdown.setText(join);
        dingMarkdown.setTitle(title);
        dingMessage.setAt(dingAt);
        dingMessage.setMarkdown(dingMarkdown);
        var url = getDingSendUrl();
        var jsonStr = JSONUtil
                .parse(dingMessage)
                .toString();
        return doSend(jsonStr, url);
    }

    @Override
    public String sendPicture(String outUrl) {
        var dingMessage = new DingMessage();
        dingMessage.setMsgtype(TYPE_MARKDOWN);
        var dingMarkdown = new DingMarkdown();
        dingMarkdown.setText("![Pic](" + outUrl + ")");
        dingMarkdown.setTitle(DEFAULT_MESSAGE_TITLE);
        dingMessage.setMarkdown(dingMarkdown);
        var url = getDingSendUrl();
        var jsonStr = JSONUtil
                .parse(dingMessage)
                .toString();
        return doSend(jsonStr, url);
    }

}

package com.xiw.kuwei.config;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "xiw.message.ding")
public class DingMessageConfig {


    private static String DEFAULT_ACCESS_TOKEN;

    private static String DEFAULT_SECRET_KEY;

    private static String MSG_ACCESS_TOKEN;

    private static String MSG_SECRET_KEY;

    private String defaultAccessToken;

    private String defaultSecretKey;

    private String msgAccessToken;

    private String msgSecretKey;

    public static String getDefaultAccessToken() {
        return DEFAULT_ACCESS_TOKEN;
    }

    public static String getDefaultSecretKey() {
        return DEFAULT_SECRET_KEY;
    }

    public static String getMsgAccessToken() {
        return MSG_ACCESS_TOKEN;
    }

    public static String getMsgSecretKey() {
        return MSG_SECRET_KEY;
    }

    @PostConstruct
    public void init() {
        DEFAULT_ACCESS_TOKEN = defaultAccessToken;
        DEFAULT_SECRET_KEY = defaultSecretKey;
        MSG_ACCESS_TOKEN = msgAccessToken;
        MSG_SECRET_KEY = msgAccessToken;
    }

}

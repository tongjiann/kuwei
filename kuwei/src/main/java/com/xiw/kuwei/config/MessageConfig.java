package com.xiw.kuwei.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "xiw.message")
public class MessageConfig {

    static boolean ENABLE = true;

    static int CHANNEL;

    boolean enable = true;

    private int channel;

    public static int getChannel() {
        return CHANNEL;
    }

    public static boolean isEnable() {
        return ENABLE;
    }

    @PostConstruct
    public void init() {
        ENABLE = enable;
        CHANNEL = channel;
    }

}

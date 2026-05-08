package com.xiw.kuwei.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "xiw.task")
public class ScheduleTaskConfig {

    static boolean ENABLE;

    private boolean enable = true;


    public static boolean isEnable() {
        return ENABLE;
    }


    @PostConstruct
    public void init() {
        ENABLE = enable;
    }


}

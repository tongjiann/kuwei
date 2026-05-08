package com.xiw.kuwei.helper.message;


import com.xiw.kuwei.task.MessageTask;

import java.util.Collection;

/**
 * @author xiwang
 * @since 2022-06-07 09:08
 */
public interface MessageHelper {

    default String buildStar(Long num) {
        if (num == null || num == 0) {
            return "";
        }
        return "★".repeat((int) Math.max(0, Math.min(5, num)));

    }

    String sendMessage(String message);

    String sendMessage(MessageTask messageTask);

    <T, M extends T> String sendMessage(T t, Class<M> clazz);

    <T, M extends T> String sendMessage(T t, Class<M> clazz, String title);

    <T, M extends T> String sendMessage(Collection<T> collection, Class<M> clazz);

    <T, M extends T> String sendMessage(Collection<T> collection, Class<M> clazz, String title);

    String sendPicture(String url);

}

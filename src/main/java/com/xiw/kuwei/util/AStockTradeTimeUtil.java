package com.xiw.kuwei.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A股交易时间工具类
 *
 * 交易时间：
 * 09:30 - 11:30
 * 13:00 - 15:00
 *
 * 节假日API：
 * https://timor.tech/api/holiday/info/{date}
 */
public class AStockTradeTimeUtil {

    /**
     * 缓存节假日信息，避免频繁调用API
     */
    private static final Map<LocalDate, Boolean> HOLIDAY_CACHE =
            new ConcurrentHashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 是否A股交易时间
     */
    public static boolean isTradingTime() {
        return isTradingTime(LocalDateTime.now());
    }

    /**
     * 是否A股交易时间
     */
    public static boolean isTradingTime(LocalDateTime dateTime) {

        LocalDate date = dateTime.toLocalDate();

        // 1. 判断是否交易日
        if (!isTradeDay(date)) {
            return false;
        }

        // 2. 判断交易时间
        LocalTime now = dateTime.toLocalTime();

        // 上午
        boolean morning =
                !now.isBefore(LocalTime.of(9, 30))
                        && now.isBefore(LocalTime.of(11, 30));

        // 下午
        boolean afternoon =
                !now.isBefore(LocalTime.of(13, 0))
                        && now.isBefore(LocalTime.of(15, 0));

        return morning || afternoon;
    }

    /**
     * 是否交易日
     */
    public static boolean isTradeDay(LocalDate date) {

        // 先判断周末
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY
                || dayOfWeek == DayOfWeek.SUNDAY) {

            // 周末可能调休
            return isWorkdayByApi(date);
        }

        // 工作日可能节假日
        return !isHolidayByApi(date);
    }

    /**
     * 是否节假日
     */
    private static boolean isHolidayByApi(LocalDate date) {

        return HOLIDAY_CACHE.computeIfAbsent(date, d -> {

            try {

                JSONObject json = requestHolidayInfo(d);

                int type = json.getJSONObject("type")
                        .getInt("type");

                /*
                 * type说明：
                 * 0 补班
                 * 1 工作日
                 * 2 周末
                 * 3 节假日
                 */

                return type == 3;

            } catch (Exception e) {

                // API异常时兜底：
                // 默认周末休市
                DayOfWeek dayOfWeek = d.getDayOfWeek();

                return dayOfWeek == DayOfWeek.SATURDAY
                        || dayOfWeek == DayOfWeek.SUNDAY;
            }
        });
    }

    /**
     * 是否补班日
     */
    private static boolean isWorkdayByApi(LocalDate date) {

        try {

            JSONObject json = requestHolidayInfo(date);

            int type = json.getJSONObject("type")
                    .getInt("type");

            return type == 0 || type == 1;

        } catch (Exception e) {

            return false;
        }
    }

    /**
     * 请求节假日API
     */
    private static JSONObject requestHolidayInfo(LocalDate date) {

        String dateStr = date.format(DATE_FORMATTER);

        String url =
                "https://timor.tech/api/holiday/info/" + dateStr;

        String result = HttpUtil.get(url);

        JSONObject root = JSONUtil.parseObj(result);

        if (root.getInt("code") != 0) {
            throw new RuntimeException("获取节假日信息失败");
        }

        return root.getJSONObject("type");
    }

    public static void main(String[] args) {

        System.out.println("是否交易时间："
                + isTradingTime());

        System.out.println("是否交易日："
                + isTradeDay(LocalDate.now()));
    }
}
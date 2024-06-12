package com.aurora.util;

import org.quartz.CronExpression;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CronUtil {

    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (Exception pe) {
            return pe.getMessage();
        }
    }

    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static String getCron(String timestamp) {
        String dateFormat = "ss mm HH dd MM ? yyyy";
        long ts = Long.parseLong(timestamp);
        Date date = new Date(ts);
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

}

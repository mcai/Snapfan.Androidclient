package snapfan.androidclient.util;

import net.pickapack.dateTime.DateHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StringHelper {
    private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public static String getFriendlyTime(long tick) {
        return getFriendlyTime(DateHelper.fromTick(tick));
    }

    public static String getFriendlyTime(Date time) {
        if (time == null) {
            return "Unknown";
        }
        String friendlyTime = "";
        Calendar cal = Calendar.getInstance();

        String curDate = dateFormater2.get().format(cal.getTime());
        String paramDate = dateFormater2.get().format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                friendlyTime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            else
                friendlyTime = hour + "小时前";
            return friendlyTime;
        }

        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                friendlyTime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            else
                friendlyTime = hour + "小时前";
        } else if (days == 1) {
            friendlyTime = "昨天";
        } else if (days == 2) {
            friendlyTime = "前天";
        } else if (days > 2 && days <= 10) {
            friendlyTime = days + "天前";
        } else if (days > 10) {
            friendlyTime = dateFormater2.get().format(time);
        }
        return friendlyTime;
    }

    public static boolean isToday(long tick) {
        return isToday(DateHelper.fromTick(tick));
    }

    public static boolean isToday(Date time) {
        boolean b = false;
        Date today = new Date();
        if (time != null) {
            String nowDate = dateFormater2.get().format(today);
            String timeDate = dateFormater2.get().format(time);
            if (nowDate.equals(timeDate)) {
                b = true;
            }
        }
        return b;
    }

    public static int toInteger(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    public static int toInteger(Object obj) {
        if (obj == null) return 0;
        return toInteger(obj.toString(), 0);
    }
}

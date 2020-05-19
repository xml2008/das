package com.ppdai.das.strategy;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TimeRangeShardLocator<CTX extends ConditionContext> extends AbstractCommonShardLocator<CTX> {
    private Range<Integer> range = null;
    private long period = 0L;
    private int field = 0;
    private final Set<Integer> bigMonths = Sets.newHashSet(1, 3, 5, 7, 8, 10, 12);

    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;
    private static final int HOUR = MINUTE * 60;
    private static final int DAY = HOUR * 24;

    TimeRangeShardLocator(TimeRangeStrategy.TIME_PATTERN timePattern) {
        switch (timePattern){
            case WEEK_OF_YEAR:
                field = Calendar.WEEK_OF_YEAR;
                range = Range.atLeast(1);
                period = DAY * 366L;
                break;
            case WEEK_OF_MONTH:
                field = Calendar.WEEK_OF_MONTH;
                range = Range.atLeast(1);
                period = DAY * 31L;
                break;
            case DAY_OF_MONTH:
                field = Calendar.DAY_OF_MONTH;
                range = Range.atLeast(1);
                period = DAY * 31L;
                break;
            case DAY_OF_YEAR:
                field = Calendar.DAY_OF_YEAR;
                range = Range.atLeast(1);
                period = DAY * 366L;
                break;
            case DAY_OF_WEEK:
                field = Calendar.DAY_OF_WEEK;
                range = Range.closed(1, 7);
                period = DAY * 7;
                break;
            case HOUR_OF_DAY:
                field = Calendar.HOUR_OF_DAY;
                range = Range.closed(0, 23);
                period = DAY;
                break;
            case MINUTE_OF_HOUR:
                field = Calendar.MINUTE;
                range = Range.closed(0, 59);
                period = HOUR;
                break;
            case SECOND_OF_MINUTE:
                field = Calendar.SECOND;
                range = Range.closed(0, 59);
                period = MINUTE;
                break;
        }
    }

    public Set<String> locateByValue(Object value) {
        Date date = parseDate(value);
        return Sets.newHashSet(toCalendarInt(date) + "");
    }

    private Date parseDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }

        if (value instanceof String) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse((String) value);
            } catch (ParseException pe) {
                throw new RuntimeException(pe);
            }
        }
        throw new RuntimeException("Sharding value is not Date or String: " + value);
    }

    private int toCalendarInt(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(field);
    }

    @Override
    public Set<String> locateForEqual(ConditionContext ctx) {
       return locateByValue(ctx.getValue());
    }

    @Override
    public Set<String> locateForGreaterThan(CTX ctx) {
        return getAllShards(ctx);
    }

    @Override
    public Set<String> locateForLessThan(CTX ctx) {
        return getAllShards(ctx);
    }

    private int getIntValue(Object value) {
        Date date = parseDate(value);
        return toCalendarInt(date);
    }

    @Override
    public Set<String> locateForBetween(ConditionContext ctx) {
        Date lowerValue = parseDate(ctx.getValue());
        Date upperValue = parseDate(ctx.getSecondValue());

        Calendar lowerCal = Calendar.getInstance();
        lowerCal.setTime(lowerValue);

        // Illegal case for between
        if(lowerValue.after(upperValue)) {
            return Sets.newHashSet();
        }

        // Cross all shards case
        if(upperValue.getTime() - lowerValue.getTime() >= period) {
            return ctx.getAllShards();
        }

        if(lowerValue.equals(upperValue)) {
            return Sets.newHashSet(toCalendarInt(lowerValue) + "");
        }

        int lowerShard = getIntValue(ctx.getValue());
        int upperShard = getIntValue(ctx.getSecondValue());

        Set<String> shards = new HashSet<>();
        if(lowerShard == upperShard) {
            shards.add(lowerShard + "");

        } else if(lowerShard < upperShard) {
           while(lowerShard <= upperShard)
               shards.add(String.valueOf(lowerShard++));

        } else {
            while(lowerShard <= upperEndpoint(lowerCal))
                shards.add(String.valueOf(lowerShard++));

            int shard = range.lowerEndpoint();
            while(shard <= upperShard)
                shards.add(String.valueOf(shard++));
        }

        return shards;
    }

    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0 || year % 400 == 0);
    }

    private int maxDayOfMonth(int year, int month) {
        if (bigMonths.contains(month)) {
            return 31;
        }
        if (month == 2) {
            if (isLeapYear(year)) {
                return 28;
            } else {
                return 29;
            }
        } else {
            return 30;
        }
    }

    private int maxDayOfYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    private int maxWeekOfYear(int year) {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.YEAR, year);
        cal1.set(Calendar.MONTH, 11);
        cal1.set(Calendar.DAY_OF_MONTH, 31);
        int w1 = cal1.get(Calendar.WEEK_OF_YEAR);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.YEAR, year);
        cal2.set(Calendar.MONTH, 11);
        cal2.set(Calendar.DAY_OF_MONTH, 24);
        int w2 = cal2.get(Calendar.WEEK_OF_YEAR);
        return Math.max(w1, w2);
    }

    private int maxWeekOfMonth(int year, int month) {
        int max = maxDayOfMonth(year, month);
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.YEAR, year);
        cal1.set(Calendar.MONTH, month - 1);
        cal1.set(Calendar.DAY_OF_MONTH, max);
        int w1 = cal1.get(Calendar.WEEK_OF_MONTH);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.YEAR, year);
        cal2.set(Calendar.MONTH, month - 1);
        cal2.set(Calendar.DAY_OF_MONTH, max - 7);
        int w2 = cal2.get(Calendar.WEEK_OF_MONTH);
        return Math.max(w1, w2);
    }

    private int upperEndpoint(Calendar cal) {
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        if (field == Calendar.DAY_OF_MONTH) {
            return maxDayOfMonth(year, month);
        }
        if (field == Calendar.DAY_OF_YEAR) {
            return maxDayOfYear(year);
        }
        if (field == Calendar.WEEK_OF_YEAR) {
            return maxWeekOfYear(year);
        }
        if (field == Calendar.WEEK_OF_MONTH) {
            return maxWeekOfMonth(year, month);
        }
        return getMaxRange();
    }

    public int getMaxRange() {
        return range.upperEndpoint();
    }
}

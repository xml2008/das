package com.ppdai.das.strategy;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class DateShardStrategy extends AbstractColumnShardStrategy {
    private static final String SELECT = "select";
    private String select = "month";//default

    @Override
    public void initialize(Map<String, String> settings) {
        super.initialize(settings);

        if(settings.containsKey(SELECT)) {
            select = settings.get(SELECT).toLowerCase();
            Preconditions.checkArgument(ImmutableSet.of("month", "week", "day").contains(select),
                    "Please configure [select] in 'month', 'week', 'day'");
        }
    }

    Date parseDate(Object value) {
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

    public static void main(String[] erwq) {

        for(int id = 1; id<100;id++){
            System.out.println(id +":"+  String.valueOf((int) ((id % 10) / 3)+1 ));
           // System.out.println(id +":"+  String.valueOf((int) ((id  / 10)+1 )));
        }

    }
    String select(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String monthOfYear = calendar.get(Calendar.YEAR) + String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        switch (this.select){
            case "month":
                return monthOfYear;
            case "week":
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
                return monthOfYear + String.format("%02d", weekOfMonth);
            case "day":
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                return monthOfYear+ String.format("%02d", dayOfMonth);
        }
        return null;
    }

    @Override
    public String calculateDbShard(Object value) {
        Date date = parseDate(value);
        return select(date);
    }

    @Override
    public String calculateTableShard(String rawTableName, Object value) {
        Date date = parseDate(value);
        return select(date);
    }
}

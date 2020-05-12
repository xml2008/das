package com.ppdai.das.strategy;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeRangeStrategy extends AbstractConditionStrategy {

    enum TIME_PATTERN {
        WEEK_OF_YEAR,
        WEEK_OF_MONTH,
        DAY_OF_MONTH,
        DAY_OF_YEAR,
        DAY_OF_WEEK,
        HOUR_OF_DAY,
        MINUTE_OF_HOUR,
        SECOND_OF_MINUTE,
    }

    /**
     * Key used to declared table shard table 0 padding
     */
    private static final String TABLE_ZERO_PADDING = "tableZeroPadding";

    /**
     * Table shard 0 padding format, default 1.
     */
    private String tableZeroPaddingFormat = "%01d";

    /**
     * Key used to declared DB shard 0 padding
     */
    private static final String ZERO_PADDING = "zeroPadding";

    /**
     * DB shard 0 padding format, default 1.
     */
    private String zeroPaddingFormat = "%01d";

    private static final String PATTERN = "pattern";
    private static final String TABLE_PATTERN = "tablePattern";

    private TimeRangeShardLocator<ConditionContext> dbLocator;
    private TimeRangeShardLocator<TableConditionContext> tableLocator;


    @Override
    public void initialize(Map<String, String> settings) {
        super.initialize(settings);

        if(isShardByDb()) {
            if(settings.containsKey(ZERO_PADDING))
                zeroPaddingFormat = "%0" + Integer.parseInt(settings.get(ZERO_PADDING)) + "d";

            dbLocator = createTimeRangeShardLocator(settings, PATTERN);
        }

        if(isShardByTable()) {

            if(settings.containsKey(TABLE_ZERO_PADDING))
                tableZeroPaddingFormat = "%0" + Integer.parseInt(settings.get(TABLE_ZERO_PADDING)) + "d";

            tableLocator = createTimeRangeShardLocator(settings, TABLE_PATTERN);

            Set<String> allShards = new HashSet<>();
            for(int i = 0; i <= tableLocator.getMaxRange(); i++)
                allShards.add(String.format(tableZeroPaddingFormat, i));

            setAllTableShards(allShards);
        }

    }

    private TimeRangeShardLocator createTimeRangeShardLocator(Map<String, String> settings, String propName) {
        Preconditions.checkArgument(settings.containsKey(propName),
                "Property " + propName + " is missing");

        String patternString = settings.get(propName);
        Optional<TIME_PATTERN> pattern = Stream.of(TIME_PATTERN.values())
                .filter(p -> p.name().equals(patternString.toUpperCase()))
                .findFirst();
        Preconditions.checkArgument(pattern.isPresent(),
                "Property " + propName + " is required within: " + TIME_PATTERN.values());
        return new TimeRangeShardLocator<>(pattern.get());
    }

    @Override
    public Set<String> locateDbShardsByValue(ShardingContext ctx, Object shardValue) {
        Set<String> original = dbLocator.locateByValue(shardValue);
        return applySuffix(original, zeroPaddingFormat);
    }

    @Override
    public Set<String> locateDbShards(ConditionContext ctx) {
        Set<String> original = dbLocator.locateShards(ctx);
        return applySuffix(original, zeroPaddingFormat);
    }

    @Override
    public Set<String> locateTableShardsByValue(TableShardingContext ctx, Object tableShardValue) {
        Set<String> original = tableLocator.locateByValue(tableShardValue);
        return applySuffix(original, tableZeroPaddingFormat);
    }

    @Override
    public Set<String> locateTableShards(TableConditionContext ctx) {
        Set<String> original = tableLocator.locateShards(ctx);
        return applySuffix(original, tableZeroPaddingFormat);
    }

    private Set<String> applySuffix(Set<String> original, String format) {
        return original.stream()
                .map(s -> String.format(format, Integer.parseInt(s)))
                .collect(Collectors.toSet());
    }

}

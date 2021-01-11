package com.ppdai.das.console.common.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtil {

    /**
     * tom(1) --> 1
     *
     * @param str
     * @return
     */
    public static String getNumber(String str) {
        Pattern pattern = Pattern.compile(".+\\((\\w+)\\).*");
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            return m.group(1);
        }
        return str;
    }

    public static <T> String joinCollectByComma(Collection<T> collection, String separator) {
        if (StringUtils.isBlank(separator)) {
            return Joiner.on(",").skipNulls().join(collection);
        } else {
            List list = new ArrayList();
            StringBuffer sb = new StringBuffer();
            collection.stream().forEach(i -> list.add("'" + i + "'"));
            return Joiner.on(",").skipNulls().join(list);
        }
    }


    public static <T> String joinCollectByComma(Collection<T> collection) {
        return Joiner.on(",").skipNulls().join(collection);
    }

    public static Set<String> toSet(String values) {
        if (StringUtils.isNotBlank(values)) {
            List<String> list = toList(values, ",");
            return new HashSet(list);
        }
        return SetUtils.EMPTY_SET;
    }

    /**
     * "a,b,c,d ---> [a,b,c,d]"
     */
    public static List<String> toList(String values) {
        if (StringUtils.isNotBlank(values)) {
            return toList(values, ",");
        }
        return ListUtils.EMPTY_LIST;
    }

    /**
     * "a,b,c,d ---> [a,b,c,d]"
     */
    public static List<Long> toLongList(String values) {
        if (StringUtils.isNotBlank(values)) {
            List<String> list = toList(values, ",");
            return toLongList(list);
        }
        return ListUtils.EMPTY_LIST;
    }

    public static List<Long> toLongList(List<String> values) {
        List<Long> rs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(values)) {
            for (String str : values) {
                rs.add(Long.valueOf(str));
            }
            return rs;
        }
        return ListUtils.EMPTY_LIST;
    }

    public static List<String> toList(String values, String separator) {
        if (StringUtils.isNotBlank(values)) {
            return Splitter.on(separator).omitEmptyStrings().trimResults().splitToList(values);
        }
        return ListUtils.EMPTY_LIST;
    }

    /**
     * "a,b,c,d ---> 'a','b','c','d'
     */
    public static String toString(String values) {
        if (StringUtils.isNotBlank(values)) {
            String regex = ",|，|\\s+";
            String strAry[] = values.split(regex);
            List<String> list = Arrays.stream(strAry).map(i -> i = "'" + i + "'").collect(Collectors.toList());
            return Joiner.on(",").skipNulls().join(list);
        }
        return StringUtils.EMPTY;
    }

    public static String getMessage(Exception e) {
        if (StringUtils.isNotBlank(e.getMessage())) {
            return e.getMessage();
        }
        return e.toString();
    }
}

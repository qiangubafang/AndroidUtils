package org.tcshare.poi;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/23.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */
import java.util.*;

/**
 * 字符串工具类，全静态方法、空安全(null-safe)，禁止实例化
 * 从apache 里抽取的，
 */
public class StringUtils {

    // ===================== 常量定义 =====================
    public static final String EMPTY = "";
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * 私有构造：禁止 new 实例
     */
    private StringUtils() {
        throw new AssertionError("Cannot instantiate utility class.");
    }

    // ===================== 空值判断 isEmpty / isBlank 核心源码 =====================
    /**
     * 判断字符串为 null 或 空串 ""
     */
    public static boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串不为 null 且 不为空串 ""
     */
    public static boolean isNotEmpty(final String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串为 null、空串、纯空白字符(空格/制表/换行)
     */
    public static boolean isBlank(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串非空、非空白
     */
    public static boolean isNotBlank(final String str) {
        return !isBlank(str);
    }

    // ===================== 裁剪 trim / strip 系列 =====================
    /**
     * 标准 trim，null 返回 null
     */
    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }

    /**
     * null 转为空串 ""，其余 trim
     */
    public static String clean(final String str) {
        return str == null ? EMPTY : str.trim();
    }

    /**
     * 去除前后空白，null 返回空串
     */
    public static String trimToEmpty(final String str) {
        return str == null ? EMPTY : str.trim();
    }

    /**
     * 去除前后空白，空白串返回 null
     */
    public static String trimToNull(final String str) {
        final String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    /**
     * 去除开头指定字符
     */
    public static String stripStart(final String str, final String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * 去除结尾指定字符
     */
    public static String stripEnd(final String str, final String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }
        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    // ===================== 字符串相等比较（空安全 equals） =====================
    public static boolean equals(final String str1, final String str2) {
        return Objects.equals(str1, str2);
    }

    public static boolean equalsIgnoreCase(final String str1, final String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    // ===================== 截取 substring 空安全 =====================
    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY;
        }
        return str.substring(start);
    }

    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return EMPTY;
        }
        return str.substring(start, end);
    }

    // ===================== 重复字符串 repeat =====================
    public static String repeat(final String str, final int repeat) {
        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return EMPTY;
        }
        final int len = str.length();
        if (len == 0 || repeat == 1) {
            return str;
        }
        final long longSize = (long) len * (long) repeat;
        final int size = (int) longSize;
        final StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < repeat; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // ===================== 分割 split 核心底层 splitWorker =====================
    public static String[] split(final String str) {
        return split(str, null, -1);
    }

    public static String[] split(final String str, String separatorChars) {
        return split(str, separatorChars, -1);
    }

    public static String[] split(final String str, String separatorChars, int max) {
        return splitWorker(str, separatorChars, max, false);
    }

    /**
     * 分割底层核心方法
     */
    private static String[] splitWorker(final String str, final String separatorChars, final int max, final boolean preserveAllTokens) {
        if (str == null) {
            return new String[0];
        }
        final int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        final List<String> list = new ArrayList<>();
        int sizePlus1 = 1;
        int i = 0;
        int start = 0;
        boolean match = false;
        boolean lastMatch = false;

        if (separatorChars == null || separatorChars.isEmpty()) {
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        match = false;
                    }
                } else {
                    lastMatch = false;
                    if (!match) {
                        start = i;
                        match = true;
                    }
                }
                i++;
            }
            if (match || (preserveAllTokens && lastMatch)) {
                list.add(str.substring(start, i));
            }
        } else {
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        match = false;
                    }
                } else {
                    lastMatch = false;
                    if (!match) {
                        start = i;
                        match = true;
                    }
                }
                i++;
            }
            if (match || (preserveAllTokens && lastMatch)) {
                list.add(str.substring(start, i));
            }
        }
        return list.toArray(new String[0]);
    }

    // ===================== 拼接 join =====================
    public static String join(final Object[] array, final String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }
        final int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        final StringBuilder buf = new StringBuilder(noOfItems * 16);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }
}
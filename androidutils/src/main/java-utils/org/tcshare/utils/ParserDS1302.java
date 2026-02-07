package org.tcshare.utils;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * DS1302传感器的年月日与java的日期互相转换
 * 24小时制
 */
public class ParserDS1302 {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("ssmmHHddMMyy");

    public static byte[] toDS1302(Date date) {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        String strDate = sdf.format(date);
        for (int i = 0; i < 12; i += 2) {
            byte ts = Byte.parseByte(strDate.substring(i, i + 1));
            byte s = Byte.parseByte(strDate.substring(i + 1, i + 2));

            byte ss = (byte) ((ts & 0x0f) << 4 | (s & 0x0f));
            buffer.put(ss);
        }

        return buffer.array();
    }

    public static Date parseDS1302(byte[] bytes) throws ParseException {
        if (bytes == null || bytes.length != 6) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            byte b = bytes[i];
            int t = (b & 0xf0) >> 4;
            int g = b & 0x0f;
            sb.append(t).append(g);
        }

        return sdf.parse(sb.toString());
    }

}

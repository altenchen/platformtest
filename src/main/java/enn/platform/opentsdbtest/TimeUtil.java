package enn.platform.opentsdbtest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author:chenchen
 * @Description:
 * @Date:2018/9/11
 * @Project:opentsdbtest
 * @Package:enn.platform.opentsdbtest
 */
public class TimeUtil {

    public static void main(String[] args) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        System.out.println("标准日期输出格式：" + date);
        System.out.println("标准日期输出格式的数据类型：" + date.getClass());
        long timeStamp = System.currentTimeMillis();
        System.out.println("时间戳：" + timeStamp);

        Date date1 = df.parse(date);
        System.out.println("date1：" + date1);
        System.out.println("date1.getTime：" + date1.getTime());

    }
}

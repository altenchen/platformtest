package enn.platform.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @Author:chenchen
 * @Description:
 * @Date:2018/9/12
 * @Project:opentsdbtest
 * @Package:enn.platform.opentsdbtest
 */
public class TimeComputing {

    public static void main(String[] args) {
        GregorianCalendar gc=new GregorianCalendar();
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = df.format(new Date());
            System.out.println("date:" + date);
            gc.setTime( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
            //秒级相加
            int runtime = 300;
            /**
             * 方法一：
             * 1 => 年
             * 2 => 月
             * 3 => 周
             * 4 => 周
             * 5 => 天
             * 10 => 小时
             * 12 => 分钟
             * 13 => 秒
             * 14 => 毫秒
             */
//            Calendar calendar = Calendar.getInstance();
//            int currentTimeInSec = calendar.get(Calendar.);
            gc.add(13, +runtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gc.getTime()));
    }
}

//            /**
//             * 方法二：
//             * 1、Calendar 输入日期转为秒
//             * 2、对秒进行累加
//             * 3、将秒转回日期格式
//             */
//            Calendar calendar = Calendar.getInstance();
////            int currentTimeInSec = calendar.get(Calendar.SECOND);
//            calendar.setTimeInMillis(System.currentTimeMillis());
//
//            System.out.println("当前秒级数据：" + calendar.get(Calendar.SECOND));
//
//
//            long timeInMillis = System.currentTimeMillis();
//            System.out.println("当前毫秒时间戳：" + timeInMillis);
//
//            Date timeInDateFormat = new Date(timeInMillis);
//            long time = timeInDateFormat.getTime();
//            System.out.println("当前毫秒时间戳转为日期：" + time);
//
//
//            long pridictedTimeStamp = time + runtime;
//            System.out.println("结束毫秒时间戳：" + pridictedTimeStamp);
//
//            //将结束时间戳转换为日期
//            Date endDate = new Date(pridictedTimeStamp);
//            String resultDate = df.format(endDate);
//            System.out.println("结束时间：" + resultDate);



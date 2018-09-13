package enn.platform.test;

/**
 * @Author:chenchen
 * @Description:
 * @Date:2018/9/12
 * @Project:opentsdbtest
 * @Package:enn.platform.test
 */
public class LockTest {

    private static int COUNT = 10;

    //全局锁改造一：使用同步代码块同步类（类锁）

//    public void handle() {
//        synchronized (LockTest.class) {
//            System.out.print("采集数据点 ");
//            COUNT--;
//            System.out.print("分析数据点=>" + COUNT + "   ");
//            System.out.print("数据点分析完了\n");
//        }
//    }

    //全局锁改造二：将方法变为静态方法
    public static synchronized void handle() {
        System.out.print("采集数据点 ");
        COUNT--;
        System.out.print("分析数据点=>" + COUNT + "   ");
        System.out.print("数据点分析完了\n");
    }

}



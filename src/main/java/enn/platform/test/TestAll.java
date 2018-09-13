package enn.platform.test;

/**
 * @Author:chenchen
 * @Description:
 * @Date:2018/9/12
 * @Project:opentsdbtest
 * @Package:enn.platform.test
 */

class TestAll{

    public static void main(String[] args) {

        Thread thread0 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                LockTest global = new LockTest();
                global.handle();
            }
        });

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                LockTest global = new LockTest();
                global.handle();
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                LockTest global = new LockTest();
                global.handle();
            }
        });

        thread0.start();
        thread1.start();
        thread2.start();

    }
}

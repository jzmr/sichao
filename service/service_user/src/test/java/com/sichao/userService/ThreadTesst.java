package com.sichao.userService;

/**
 * @Description:
 * @author: sjc
 * @createTime: 2023年05月01日 16:23
 */
public class ThreadTesst {
    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();

    public static void main(String[] args) throws InterruptedException {

        //一号线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("一号线程set前：" + threadLocal.get());
                threadLocal.set(1);
                System.out.println("一号线程set后：" + threadLocal.get());
            }
        }).start();

        //二号线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("二号线程set前：" + threadLocal.get());
                threadLocal.set(2);
                System.out.println("二号线程set后：" + threadLocal.get());

            }
        }).start();

        //主线程睡1s
        Thread.sleep(1000);

        //主线程
        System.out.println("主线程的threadlocal值：" + threadLocal.get());

    }
}

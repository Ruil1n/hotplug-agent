package cn.rui0.javaagent.weavers.asm;

/**
 * Created by ruilin on 2019/7/9.
 */

public class InvokeTimer {
    static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();


    public static void start() {
        threadLocal.set(System.currentTimeMillis());
        System.err.println("start");
    }

    public static void end() {
        long time = System.currentTimeMillis() - threadLocal.get();
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        System.err.println("end");
        System.err.println("Class name:"+element.getClassName() +",Method name:"+ element.getMethodName() + ",Line number:"+ element.getLineNumber()+ ",耗费时间:" + time + "ms.");
    }
}

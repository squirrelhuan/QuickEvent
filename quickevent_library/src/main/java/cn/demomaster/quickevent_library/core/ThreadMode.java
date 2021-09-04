package cn.demomaster.quickevent_library.core;

/**
 * LKXEventBus接收事件的方法在哪个线程运行
 */
public enum ThreadMode {

    /**
     * 发送方法在哪个线程运行，接收方法就在哪个线程执行
     */
    POSTING,

    /**
     * 接收方法在主线程运行
     */
    MAIN,

    /**
     * 接收方法在子线程运行
     */
    ASYNC,



}

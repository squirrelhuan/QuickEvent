package cn.demomaster.quickevent_library.core;

import java.lang.reflect.Method;

/**
 * 注册类的方法信息
 */
public class SubscribeMethod {

    /**
     * 注册的方法
     */
    public Method method;

    /**
     * 线程类型
     *
     */
    public ThreadMode threadMode;

    /**
     * 参数类型
     */
    public Class<?> paramsType;

    public SubscribeMethod(Method method, ThreadMode threadMode, Class<?> paramsType) {
        this.method = method;
        this.threadMode = threadMode;
        this.paramsType = paramsType;
    }
}

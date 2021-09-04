package cn.demomaster.quickevent_library.core;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 手写EventBus
 */

public class QuickEvent {

    private static QuickEvent eventBus = new QuickEvent();

    /**
     * 缓存注册方法的Map
     * @Object 注册者
     * @List 注册者中注册方法的集合
     */
    private static Map<Object, List<SubscribeMethod>> cacheMap;

    private Handler handler;

    //线程池
    private ExecutorService executorService;

    private QuickEvent(){
        cacheMap = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());
        executorService = Executors.newCachedThreadPool();
    }

    public static QuickEvent getDefault(){
        return eventBus;
    }

    /**
     * 方法的注册
     * 1.从方法集合中查找是否已经注册过就不需要注册
     * 2.如果没有注册则遍历并添加注册的方法到集合中
     * @param subscriber
     */
    public void register(Object subscriber){
        List<SubscribeMethod> subscribeMethods = cacheMap.get(subscriber);
        if (subscribeMethods == null) {
            subscribeMethods = getSubscribeMethods(subscriber);
            cacheMap.put(subscriber,subscribeMethods);
        }
    }

    /**
     * 获取注册者中的注册方法集合
     * @param subscriber
     * @return
     */
    private List<SubscribeMethod> getSubscribeMethods(Object subscriber) {
        List<SubscribeMethod> subscribeMethodList = new ArrayList<>();
        Class<?> currentClass = subscriber.getClass();
        //subscriber--->BaseActivity--->Activity
        while (currentClass!=null){
            String name = currentClass.getName();
            if (name.startsWith("java.")
                    ||name.startsWith("javax.")
                    ||name.startsWith("android.")
                    || name.startsWith("androidx.")){
            break;
            }

            Method[] declaredMethods = currentClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                Subscribe annotation = declaredMethod.getAnnotation(Subscribe.class);
                if (annotation == null) {
                    continue;
                }
                //获取方法的参数类型，注册的方法参数只能有一个Object类型
                Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                if (parameterTypes.length !=1){
                    throw new RuntimeException("EventBus注册的方法只能接受一个参数");
                }

                //符合要求的方法,加入集合
                ThreadMode threadMode = annotation.threadMode();
                SubscribeMethod method = new SubscribeMethod(declaredMethod,threadMode,parameterTypes[0]);
                subscribeMethodList.add(method);
            }
            currentClass = currentClass.getSuperclass();
        }
        return subscribeMethodList;
    }

    /**
     * 事件发送
     * 1.遍历Map拿到已经缓存的方法集合
     * 2.遍历已经注册的方法集合
     * 3.判断已注册方法的参数类型是否等于参数object的类型或者父类
     * 4.如果是则反射调用注册的方法，并将object作为方法参数
     * @param object
     */
    public void post(Object object){
        Set<Object> keySet = cacheMap.keySet();
        for (Object subscriber : keySet) {
            List<SubscribeMethod> subscribeMethods = cacheMap.get(subscriber);
            for (SubscribeMethod subscribeMethod : subscribeMethods) {
                Class<?> paramsType = subscribeMethod.paramsType;
                Class<?> objectClass = object.getClass();
                if (paramsType.isAssignableFrom(objectClass)){
                    invoke(subscribeMethod, subscriber, object);
                }
            }
        }
    }
    public void post(Class clazz, Object object){
        Set<Object> keySet = cacheMap.keySet();
        for (Object subscriber : keySet) {
            List<SubscribeMethod> subscribeMethods = cacheMap.get(subscriber);
            for (SubscribeMethod subscribeMethod : subscribeMethods) {
                Class cla = subscribeMethod.method.getDeclaringClass();
                if(cla == clazz){
                    Class<?> paramsType = subscribeMethod.paramsType;
                    Class<?> objectClass = object.getClass();
                    if (paramsType.isAssignableFrom(objectClass)){
                        invoke(subscribeMethod, subscriber, object);
                    }
                }
            }
        }
    }

    /**
     *
     * @param subscribeMethod 注册方法
     * @param subscriber 注册者
     * @param obj 参数
     */
    private void invoke (final SubscribeMethod subscribeMethod, final Object subscriber, final Object obj){
        try {
            switch (subscribeMethod.threadMode) {
                case MAIN:
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        subscribeMethod.method.invoke(subscriber, obj);
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    subscribeMethod.method.invoke(subscriber, obj);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    break;
                case POSTING:
                    subscribeMethod.method.invoke(subscriber, obj);
                    break;
                case ASYNC:
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    subscribeMethod.method.invoke(subscriber, obj);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        subscribeMethod.method.invoke(subscriber, obj);
                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 取消注册,将已注册的方法移除
     * @param subscriber
     */
    public void unRegister(Object subscriber){
        List<SubscribeMethod> subscribeMethodList = cacheMap.get(subscriber);
        if (subscribeMethodList != null) {
            cacheMap.remove(subscriber);
        }
    }

}

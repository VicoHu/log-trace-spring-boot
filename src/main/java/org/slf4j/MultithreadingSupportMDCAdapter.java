package org.slf4j;

import org.slf4j.spi.MDCAdapter;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 多线程支持的MDC适配器
 *
 * @author: VicoHu
 * @date: 2024/7/31
 */
public class MultithreadingSupportMDCAdapter implements MDCAdapter {
    /**
     * 使用阿里的TransmittableThreadLocal来实现线程间的数据传递
     */
    final ThreadLocal<Map<String, String>> copyOnInheritThreadLocal = new TransmittableThreadLocal<>();

    private static final int WRITE_OPERATION = 1;
    private static final int READ_OPERATION = 2;

    private static MultithreadingSupportMDCAdapter multithreadingSupportMDCAdapter;

    /**
     * 用于跟踪上次执行的操作
     */
    final ThreadLocal<Integer> lastOperation = new ThreadLocal<>();

    static {
        multithreadingSupportMDCAdapter = new MultithreadingSupportMDCAdapter();
        // MDC.mdcAdapter 权限问题
        MDC.mdcAdapter = multithreadingSupportMDCAdapter;
        System.out.println("MultithreadingSupportMDCAdapter initialized");
    }

    public static MDCAdapter getInstance() {
        return multithreadingSupportMDCAdapter;
    }

    private Integer getAndSetLastOperation(int op) {
        Integer lastOp = lastOperation.get();
        lastOperation.set(op);
        return lastOp;
    }

    private static boolean wasLastOpReadOrNull(Integer lastOp) {
        return lastOp == null || lastOp == READ_OPERATION;
    }

    private Map<String, String> duplicateAndInsertNewMap(Map<String, String> oldMap) {
        Map<String, String> newMap = Collections.synchronizedMap(new HashMap<>());
        if (oldMap != null) {
            // 由于不希望父线程修改oldMap，所以这里需要加锁
            synchronized (oldMap) {
                // 遍历 oldMap，并将其内容复制到 newMap 中
                newMap.putAll(oldMap);
            }
        }

        copyOnInheritThreadLocal.set(newMap);
        return newMap;
    }

    /**
     * 将上下文值（<code>val</code> 参数）与
     * <code>key</code> 参数传入当前线程的上下文映射。注意
     * 与 log4j 相反，<code>val</code> 参数可以为 null。
     * <p/>
     * <p/>
     * 如果当前线程没有上下文映射，则将其创建为一侧
     * 此调用的效果。
     * 如果"key"参数为 null，抛出 IllegalArgumentException
     */
    @Override
    public void put(String key, String val) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        Map<String, String> oldMap = copyOnInheritThreadLocal.get();
        Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

        if (wasLastOpReadOrNull(lastOp) || oldMap == null) {
            Map<String, String> newMap = duplicateAndInsertNewMap(oldMap);
            newMap.put(key, val);
        } else {
            oldMap.put(key, val);
        }
    }

    /**
     * 删除由<code>key</code>参数标识的上下文。
     */
    @Override
    public void remove(String key) {
        if (key == null) {
            return;
        }
        Map<String, String> oldMap = copyOnInheritThreadLocal.get();
        if (oldMap == null) {
            return;
        }

        Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

        if (wasLastOpReadOrNull(lastOp)) {
            Map<String, String> newMap = duplicateAndInsertNewMap(oldMap);
            newMap.remove(key);
        } else {
            oldMap.remove(key);
        }

    }


    /**
     * 清除MDC 中的所有条目。
     */
    @Override
    public void clear() {
        lastOperation.set(WRITE_OPERATION);
        copyOnInheritThreadLocal.remove();
    }

    /**
     * 获取由<code>key</code>参数标识的上下文。
     */
    @Override
    public String get(String key) {
        Map<String, String> map = getPropertyMap();
        if ((map != null) && (key != null)) {
            return map.get(key);
        } else {
            return null;
        }
    }

    /**
     * 获取当前线程的MDC作为映射。该方法仅用于内部调用
     */
    public Map<String, String> getPropertyMap() {
        lastOperation.set(READ_OPERATION);
        return copyOnInheritThreadLocal.get();
    }


    /**
     * 返回当前线程上下文映射的副本。返回值可能为null
     */
    @Override
    public Map<String, String> getCopyOfContextMap() {
        lastOperation.set(READ_OPERATION);
        Map<String, String> hashMap = copyOnInheritThreadLocal.get();
        if (hashMap == null) {
            return null;
        } else {
            return new HashMap<>(hashMap);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setContextMap(Map contextMap) {
        lastOperation.set(WRITE_OPERATION);

        Map<String, String> newMap = Collections.synchronizedMap(new HashMap<>());
        newMap.putAll(contextMap);

        // 为了确保序列化的准确性，需要 newMap 替换 oldMap
        copyOnInheritThreadLocal.set(newMap);


    }

}

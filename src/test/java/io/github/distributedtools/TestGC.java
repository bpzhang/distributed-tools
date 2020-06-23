package io.github.distributedtools;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestGC {

    public Object instance = null;
    private static final int _1MB = 1024 * 1024;
    /***这个成员属性的唯一意义就是占点内存，以便能在GC日志中看清楚是否有回收过*/
    private byte[] bigSize = new byte[2 * _1MB];

    public static void testGC() {
        TestGC objA = new TestGC();
        TestGC objB = new TestGC();
        objA.instance = objB;
        objB.instance = objA;
        objA = null;
        objB = null;
        //假设在这行发生GC，objA和objB是否能被回收？S
        System.gc();

    }

    public static void main(String[] args) {
        Map<String,String> curMap = new ConcurrentHashMap<>(3);
        curMap.put("key_" + 0, String.valueOf(0));
        curMap.put("key_" + 1, String.valueOf(1));
        curMap.put("key_" + 2, String.valueOf(2));
        curMap.put("key_" + 3, String.valueOf(3));
        curMap.put("key_" + 4, String.valueOf(4));
        curMap.put("key_" + 5, String.valueOf(5));
        curMap.put("key_" + 6, String.valueOf(6));
        curMap.put("key_" + 20, String.valueOf(7));
        curMap.put("key_" + 8, String.valueOf(8));
        curMap.put("key_" + 18, String.valueOf(8));
        curMap.put("key_" + 28, String.valueOf(8));
        curMap.put("key_" + 38, String.valueOf(8));
        curMap.put("key_" + 48, String.valueOf(8));


        String s = curMap.get("key_" + 38);
        System.out.println(s);
    }
}
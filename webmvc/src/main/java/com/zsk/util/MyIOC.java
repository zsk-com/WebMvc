package com.zsk.util;

import java.util.HashMap;

/**
 * 管理对象类 且为生命周期模式
 */
public class MyIOC {

    private static HashMap<String,Object> user=new HashMap<>();

    /**
     *
     * @param className 需要管理类的全路径
     * @return 对象Object
     */
    public static Object getBean(String className){
        Object obj=null;
        try {
            Class clazz=  Class.forName(className);
           obj=user.get(className);
           if (obj==null){
               obj=clazz.newInstance();
               user.put(className,obj);
           }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return obj;
    }
}

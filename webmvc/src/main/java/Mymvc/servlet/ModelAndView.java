package Mymvc.servlet;

import java.util.HashMap;

public class ModelAndView {
    //存requset
    private static HashMap<String,String> requsetmap=new HashMap();

    //存response
    private static HashMap<String,String> sessionmap=new HashMap();
    //存json数据
//    private static HashMap<String,Object> jsonmap=new HashMap<>();

    //给用户用
    //requset
    public void setRequsetAttribute(String key,String value){
        requsetmap.put(key,value);
    }

    String getRequsetAttribute(String key){
        return requsetmap.get(key);
    }
    HashMap<String,String> getRequsetAll(){
        return this.requsetmap;
    }
    //response
    public void setsessionAttribute(String key,String value){
        sessionmap.put(key,value);
    }
    String getSessionAttribute(String key){
        return sessionmap.get(key);
    }
    HashMap<String,String> getSessionAll(){
        return this.sessionmap;
    }

//    //json
//    public void addJSON(String key,Object obj){
//        jsonmap.put(key,obj);
//    }
//
//    Object setJSON(){
//        return jsonmap;
//    }
}

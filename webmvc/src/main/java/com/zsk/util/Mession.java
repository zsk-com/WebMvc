package com.zsk.util;
import com.google.gson.Gson;

public class Mession {
    private String ruselt;
    private Object  date;
    public String toJSON(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }
    public Mession(Object date){
        this.date=date;
    }
    public Mession(){}
    public Mession(String ruselt){
        this.ruselt=ruselt;
    }
    public Mession(String ruselt,Object date){
        this.ruselt=ruselt;
        this.date=date;
    }

    public String getRuselt() {
        return ruselt;
    }

    public void setRuselt(String ruselt) {
        this.ruselt = ruselt;
    }

    public Object getDate() {
        return date;
    }

    public void setDate(Object date) {
        this.date = date;
    }
}

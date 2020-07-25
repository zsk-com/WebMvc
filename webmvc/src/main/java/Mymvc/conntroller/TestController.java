package Mymvc.conntroller;

import Mymvc.annotation.Param;

public class TestController {

    public  String test(){

        return "success";
    }


    public  String test2(@Param("name") String name, @Param("pass")String pass){
        System.out.println("接收到参数啦"+name+"---"+pass);
        return "success";
    }
}

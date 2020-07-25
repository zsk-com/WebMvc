package Mymvc.conntroller;

import Mymvc.annotation.RequestMapping;
import Mymvc.annotation.ResponseBody;

@RequestMapping("/demo")
public class DemoController {
    @ResponseBody
    @RequestMapping("/des.do")
    public String demo(){

        return "success";
    }
}

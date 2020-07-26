package com.zsk.conntroller;

import com.zsk.annotation.RequestMapping;
import com.zsk.annotation.ResponseBody;
import com.zsk.servlet.ModelAndView;

@RequestMapping("/demo")
public class DemoController {
    @ResponseBody
    @RequestMapping("/des.do")
    public String demo(){
        ModelAndView model=new ModelAndView();
        model.setRequsetAttribute("key","value");
        model.setsessionAttribute("key","value");
        return "success";
    }
}

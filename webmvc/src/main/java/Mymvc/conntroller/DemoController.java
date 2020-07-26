package Mymvc.conntroller;

import Mymvc.annotation.RequestMapping;
import Mymvc.annotation.ResponseBody;
import Mymvc.servlet.ModelAndView;

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

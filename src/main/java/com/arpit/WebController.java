package com.arpit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Controller
public class WebController
{

    @RequestMapping(value={"/index","","/"},method = RequestMethod.GET)
    public String blank_index()
    {
        return "/chatpage.jsp";
    }


}
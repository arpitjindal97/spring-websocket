package com.arpit;
import org.springframework.stereotype.*;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.io.*;
import java.net.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


@Controller
public class WebController
{

    @RequestMapping(value={"/index","","/"},method = RequestMethod.GET)
    public String blank_index()
    {
        return "chat.html";
    }


}
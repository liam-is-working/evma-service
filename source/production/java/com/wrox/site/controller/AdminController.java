package com.wrox.site.controller;

import com.wrox.config.annotation.WebController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@WebController
public class AdminController {
    @RequestMapping(value = "test", method = RequestMethod.GET)
    public String test(){
        return "test";
    }

//    @RequestMapping(value = "login", method = RequestMethod.GET)
//    public String login(){
//
//    }
}

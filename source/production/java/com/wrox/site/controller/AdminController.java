package com.wrox.site.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {
    @RequestMapping(value = "test")
    public String test(){
        return "test";
    }
}

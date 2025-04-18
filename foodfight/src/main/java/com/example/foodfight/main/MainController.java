package com.example.foodfight.main;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/index")
    @ResponseBody
    public String index() {
        return "index";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/upload/list";//리다이렉트 기본주소 입력시 항상 여기로 옴
    }
}
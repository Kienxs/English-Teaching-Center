package com.example.English.teaching.center.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserPageController {
    @GetMapping("/home")
    public String userHome() {
        return "user/home";
    }

    @GetMapping("/courseList")
    public String courseList(){
        return "user/courseList";
    }

    @GetMapping("/teacher")
    public String teacher(){
        return "user/teacher";
    }

    @GetMapping("/my-course")
    public String learn(){
        return "user/my-course";
    }

    @GetMapping("/news")
    public String news(){
        return "user/news";
    }

    @GetMapping("/blog")
    public String blog(){
        return "user/blog";
    }
}

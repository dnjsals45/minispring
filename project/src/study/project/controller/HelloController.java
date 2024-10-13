package study.project.controller;

import study.minispringframework.annotation.Controller;
import study.minispringframework.annotation.RequestMapping;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/test")
    public String helloTest() {
        return "Hello, World!";
    }
}

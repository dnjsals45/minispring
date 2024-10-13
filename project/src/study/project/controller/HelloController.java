package study.project.controller;

import study.minispringframework.annotation.Controller;
import study.minispringframework.annotation.RequestMapping;
import study.project.service.HelloService;

@Controller
@RequestMapping("/hello")
public class HelloController {
    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @RequestMapping("/test")
    public String helloTest() {
        return helloService.hello();
    }
}

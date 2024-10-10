package project;

import minispringframework.annotation.Controller;
import minispringframework.annotation.RequestMapping;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/test")
    public String helloTest() {
        return "Hello, World!";
    }
}

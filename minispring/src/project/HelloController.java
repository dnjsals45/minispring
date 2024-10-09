package project;

import minispringframework.annotation.Controller;
import minispringframework.annotation.RequestMapping;

@Controller
public class HelloController {

    @RequestMapping("/hello")
    public String helloTest() {
        return "Hello, World!";
    }
}

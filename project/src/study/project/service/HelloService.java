package study.project.service;

import study.minispringframework.annotation.Service;

@Service
public class HelloService {
    public String hello() {
        return "Hello, World!";
    }
}

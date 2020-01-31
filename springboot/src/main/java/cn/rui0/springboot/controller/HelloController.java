package cn.rui0.springboot.controller;

import cn.rui0.springboot.exceptions.HelloException;
import cn.rui0.springboot.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/index")
public class HelloController {

    @Autowired
    private HelloService helloService;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String helloIndex() {
        return helloService.helloService();
    }

    @RequestMapping(value = "/exception", method = RequestMethod.GET)
    public void helloException() {
        try {
            helloService.helloException();
        } catch (HelloException e) {

        }
    }
}

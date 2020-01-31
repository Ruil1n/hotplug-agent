package cn.rui0.springboot.service;

import cn.rui0.springboot.exceptions.HelloException;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String helloService() {
        return "Hello!";
    }

    public void helloException() throws HelloException {
        throw new HelloException("There is a HelloException");
    }
}

package com.juan.test.spring.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableResourceServer
public class SampleAuthorizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleAuthorizationServerApplication.class, args);
    }
    
    @RequestMapping("/")
    public String home() {
    	return "Sample OAuth2 application\n";
    }
}

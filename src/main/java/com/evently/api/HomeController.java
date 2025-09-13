package com.evently.api;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseStatus(HttpStatus.FOUND)
    public String root() {
        return "redirect:/swagger-ui/index.html";
    }
}

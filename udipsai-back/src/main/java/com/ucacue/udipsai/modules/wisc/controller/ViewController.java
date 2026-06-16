package com.ucacue.udipsai.modules.wisc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping({"/", "/index.html"})
    public String index() {
        return "index";
    }



    @GetMapping("/modulos/{nombre}")
    public String getModulo(@PathVariable String nombre) {
        return "modules/" + nombre;
    }
}

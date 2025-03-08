package com.juege.tech_doc.controller;

import com.juege.tech_doc.domain.Test;
import com.juege.tech_doc.service.DemoService;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Resource
    private DemoService demoService;

    @GetMapping("/list")
    public List<Test> list() {
        return demoService.list();
    }
}

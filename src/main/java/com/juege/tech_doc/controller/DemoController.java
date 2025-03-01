package com.juege.tech_doc.controller;

import com.juege.tech_doc.domain.TestTable;
import com.juege.tech_doc.service.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Resource
    private DemoService demoService;

    @GetMapping("/list")
    public List<TestTable> list() {
        return demoService.list();
    }
}

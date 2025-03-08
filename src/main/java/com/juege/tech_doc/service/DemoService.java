package com.juege.tech_doc.service;

import java.util.List;

import com.juege.tech_doc.domain.Test;
import com.juege.tech_doc.mapper.DemoMapper;
import com.juege.tech_doc.mapper.TestMapper;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

@Service
public class DemoService {

    @Resource
    private DemoMapper demoMapper;

    @Resource
    private TestMapper testMapper;

    public List<Test> list() {
//        return demoMapper.selectByExample(null);
        return testMapper.selectList(null);
    }
}

package com.juege.tech_doc.service;

import com.juege.tech_doc.domain.Test;
import com.juege.tech_doc.mapper.TestMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TestService {

    @Resource
    private TestMapper testMapper;

    public List<Test> list() {
        return testMapper.list();
    }
}

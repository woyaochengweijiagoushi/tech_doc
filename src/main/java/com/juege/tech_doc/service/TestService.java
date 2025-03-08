package com.juege.tech_doc.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.juege.tech_doc.domain.Test;
import com.juege.tech_doc.mapper.TestMapper;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.List;

@Service
public class TestService {

    @Resource
    private TestMapper testMapper;

    public List<Test> list() {
        return testMapper.selectList(Wrappers.emptyWrapper());
    }
}

package com.juege.tech_doc.service;

import com.juege.tech_doc.domain.TestTable;
import com.juege.tech_doc.mapper.DemoMapper;
import com.juege.tech_doc.mapper.TestTableMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DemoService {

    @Resource
    private DemoMapper demoMapper;

    @Resource
    private TestTableMapper testTableMapper;

    public List<TestTable> list() {
//        return demoMapper.selectByExample(null);
        return testTableMapper.selectByExample(null);
    }
}

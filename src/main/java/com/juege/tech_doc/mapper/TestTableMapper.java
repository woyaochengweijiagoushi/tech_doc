package com.juege.tech_doc.mapper;

import com.juege.tech_doc.domain.TestTable;
import com.juege.tech_doc.domain.TestTableExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestTableMapper {
    long countByExample(TestTableExample example);

    int deleteByExample(TestTableExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TestTable record);

    int insertSelective(TestTable record);

    List<TestTable> selectByExampleWithBLOBs(TestTableExample example);

    List<TestTable> selectByExample(TestTableExample example);

    TestTable selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TestTable record, @Param("example") TestTableExample example);

    int updateByExampleWithBLOBs(@Param("record") TestTable record, @Param("example") TestTableExample example);

    int updateByExample(@Param("record") TestTable record, @Param("example") TestTableExample example);

    int updateByPrimaryKeySelective(TestTable record);

    int updateByPrimaryKeyWithBLOBs(TestTable record);

    int updateByPrimaryKey(TestTable record);
}
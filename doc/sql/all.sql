USE wiki;

-- 判断表是否存在，若存在则删除
DROP TABLE IF EXISTS test_table;

-- 创建新表
CREATE TABLE test_table (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

select * from test_table

package com.ruoyi.generator.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName ExcelTable
 * @Author Jumy
 * @Description //TODO
 * @Date 2020/10/5 14:28
 * @Version 1.0
 **/
@Data
public class ExcelTable {
    /** 表名称 */
    @ExcelProperty(value="表名称")
    private String tableName;

    /** 表描述 */
    @ExcelProperty(value="表名称")
    private String tableComment;

    public ExcelTable() {
    }

    public ExcelTable(String tableName, String tableComment) {
        this.tableName = tableName;
        this.tableComment = tableComment;
    }
}

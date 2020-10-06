package com.ruoyi.generator.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

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

    /** 模块名 */
    @ExcelProperty(value="模块名")
    private String moduleName;

    /** 表列 */
    private List<ExcelColumn> columnList;

    public ExcelTable() {
    }

    public ExcelTable(String tableName, String tableComment) {
        this.tableName = tableName;
        this.tableComment = tableComment;
    }

    public ExcelTable(String tableName, String tableComment, String moduleName) {
        this.tableName = tableName;
        this.tableComment = tableComment;
        this.moduleName = moduleName;
    }
}

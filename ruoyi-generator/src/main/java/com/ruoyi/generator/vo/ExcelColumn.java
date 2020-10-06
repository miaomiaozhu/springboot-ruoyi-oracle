package com.ruoyi.generator.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 代码生成业务字段表 gen_table_column
 * 
 * @author ruoyi
 */
@Data
public class ExcelColumn extends BaseRowModel
{
    private static final long serialVersionUID = 1L;

    /** 字段名称 */
    @ExcelProperty(value="字段名称",index = 0)
    private String columnName;

    /** 字段中文名称 */
    @ExcelProperty(value="字段中文名称",index = 1)
    private String columnComment;

    /** 字段类型 */
    @ExcelProperty(value="字段类型",index = 2)
    private String columnType;

    /** 是否主键（1是） */
    @ExcelProperty(value="是否主键",index = 3)
    private String isPk;

    /** 备注 */
    @ExcelProperty(value="备注",index = 4)
    private String memo;

    /** JAVA类型 */
    private String javaType;

    /** JAVA字段名 */
    @NotBlank(message = "Java属性不能为空")
    private String javaField;


    /** 是否自增（1是） */
    private String isIncrement;

    /** 是否必填（1是） */
    private String isRequired;

    /** 是否为插入字段（1是） */
    private String isInsert;

    /** 是否编辑字段（1是） */
    private String isEdit;

    /** 是否列表字段（1是） */
    private String isList;

    /** 是否查询字段（1是） */
    private String isQuery;

    /** 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围） */
    private String queryType;

    /** 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、upload上传控件、summernote富文本控件） */
    private String htmlType;

    /** 字典类型 */
    private String dictType;

    /** 排序 */
    private Integer sort;

    public ExcelColumn() {
    }

    public ExcelColumn(String columnName, String columnComment, String columnType, String isPk) {
        this.columnName = columnName;
        this.columnComment = columnComment;
        this.columnType = columnType;
        this.isPk = isPk;
    }
}
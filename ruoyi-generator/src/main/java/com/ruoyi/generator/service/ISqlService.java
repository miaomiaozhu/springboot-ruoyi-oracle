package com.ruoyi.generator.service;

import com.ruoyi.generator.vo.ExcelColumn;
import com.ruoyi.generator.vo.ExcelTable;

import java.util.List;

/**
 * 业务 服务层
 *
 * @author ruoyi
 */
public interface ISqlService {
    /**
     * 查询业务列表
     *
     * @param columnList 列信息
     * @return
     */
    public String generateSql(ExcelTable excelTable, List<ExcelColumn> columnList);

}

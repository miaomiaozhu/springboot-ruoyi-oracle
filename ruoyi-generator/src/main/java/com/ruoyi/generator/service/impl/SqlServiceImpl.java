package com.ruoyi.generator.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.GenConstants;
import com.ruoyi.common.core.text.CharsetKit;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.BusinessException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;
import com.ruoyi.generator.mapper.GenTableColumnMapper;
import com.ruoyi.generator.mapper.GenTableMapper;
import com.ruoyi.generator.service.IGenTableService;
import com.ruoyi.generator.service.ISqlService;
import com.ruoyi.generator.sql.SqlConstants;
import com.ruoyi.generator.util.GenUtils;
import com.ruoyi.generator.util.VelocityInitializer;
import com.ruoyi.generator.util.VelocityUtils;
import com.ruoyi.generator.vo.ExcelColumn;
import com.ruoyi.generator.vo.ExcelTable;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 业务 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class SqlServiceImpl implements ISqlService {
    private static final Logger log = LoggerFactory.getLogger(SqlServiceImpl.class);

    @Override
    public String generateSql(ExcelTable excelTable, List<ExcelColumn> columnList) {
        String sqlTemplate = SqlConstants.SQL_TEMPLATE;
        String pkTemplate = SqlConstants.PK_TEMPLATE;
        String seqTemplate = SqlConstants.SEQ_TEMPLATE;
        String columnDefineListTemplate = SqlConstants.COLUMN_DEFINE_LIST_TEMPLATE;
        String columnCommentListTemplate = SqlConstants.COLUMN_COMMENT_LIST_TEMPLATE;
        StringBuilder sql=new StringBuilder();
        //列定义
        StringBuilder columnDefineList=new StringBuilder();
        //列注释
        StringBuilder columnCommentList=new StringBuilder();
        //主键sql
        StringBuilder primaryKeySql=new StringBuilder();
        //序列sql
        StringBuilder sequenceSql=new StringBuilder();
        //主键字段
        StringBuilder primary_key=new StringBuilder();
        //遍历列，组装所有列定义和列注释信息
        for (ExcelColumn excelColumn:columnList) {
            columnDefineList.append(columnDefineListTemplate
                    .replaceAll("<<COLUMN_NAME>>", excelColumn.getColumnName())
                    .replaceAll("<<COLUMN_TYPE>>", excelColumn.getColumnType()));
            columnCommentList.append(columnCommentListTemplate
                    .replaceAll("<<TABLE_NAME>>", excelTable.getTableName())
                    .replaceAll("<<COLUMN_NAME>>", excelColumn.getColumnName())
                    .replaceAll("<<COLUMN_COMMENT>>", excelColumn.getColumnComment()));
            if (!StringUtils.isEmpty(excelColumn.getIsPk())&&SqlConstants.IS_PK_SCOPE.contains(excelColumn.getIsPk())) {
                primary_key.append(excelColumn.getColumnName()+",");
            }
        }
        if (!StringUtils.isEmpty(primary_key)&&primary_key.toString().endsWith(",")) {
            primary_key = primary_key.deleteCharAt(primary_key.length()-1);
        }
        if (columnDefineList.toString().endsWith(", \n")) {
            columnDefineList=columnDefineList.deleteCharAt(columnDefineList.lastIndexOf(","));
        }
        //替换表名，表注释，列定义和列注释替换符
        sql=sql.append(sqlTemplate.
                replaceAll("<<TABLE_NAME>>", excelTable.getTableName())
                .replaceAll("<<TABLE_COMMENT>>", excelTable.getTableComment())
                .replaceAll("<<COLUMN_DEFINE_LIST>>", columnDefineList.toString())
                .replaceAll("<<COLUMN_COMMENT_LIST>>", columnCommentList.toString()));
        //有主键的话，增加主键约束
        if (!StringUtils.isEmpty(primary_key.toString())) {
            //替换主键信息
            primaryKeySql.append(pkTemplate.replaceAll("<<TABLE_NAME>>",excelTable.getTableName()).replaceAll("<<PRIMARY_KEY>>",primary_key.toString()));
            sql.append(primaryKeySql);
        }
        //序列信息
        sequenceSql.append(seqTemplate.replaceAll("<<TABLE_NAME>>",excelTable.getTableName()));
        sql.append(sequenceSql);
        return sql.toString();
    }
}
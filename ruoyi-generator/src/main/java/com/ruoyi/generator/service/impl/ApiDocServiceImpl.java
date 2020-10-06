package com.ruoyi.generator.service.impl;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.generator.config.OperateConstants;
import com.ruoyi.generator.config.OperateEnum;
import com.ruoyi.generator.config.PageColumnEnum;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;
import com.ruoyi.generator.easyexcel.ExcelListener;
import com.ruoyi.generator.mapper.GenTableMapper;
import com.ruoyi.generator.service.IApiDocService;
import com.ruoyi.generator.sql.SqlConstants;
import com.ruoyi.generator.util.GenUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 业务 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class ApiDocServiceImpl implements IApiDocService {
    private static final Logger log = LoggerFactory.getLogger(ApiDocServiceImpl.class);
    //表头样式
    public static HSSFCellStyle cellStyle_title;
    //内容样式
    public static HSSFCellStyle cellStyle_content;
    //sheet
    public static HSSFSheet sheet;
    //行
    public static HSSFRow row;
    //单元格
    public static HSSFCell cell;
    //当前行数
    public static int rowIndex;
    @Autowired
    private GenTableMapper genTableMapper;

    @Override
    public HSSFWorkbook generateApiDoc(MultipartFile file) {
        List<GenTable> tableList = readExcel(file);
        //初始化列信息
        for (GenTable table : tableList)
        {
            {
                for (GenTableColumn column : table.getColumns())
                {
                    //提取数据库数据类型
                    String columnType = column.getColumnType().toLowerCase();
                    if (columnType.contains("(")) {
                        columnType=columnType.substring(0,columnType.lastIndexOf("("));
                    }
                    column.setColumnType(columnType);
                    if (!StringUtils.isEmpty(column.getIsPk())&&SqlConstants.IS_PK_SCOPE.contains(column.getIsPk())) {
                        table.setPkColumn(column);
                    }
                    GenUtils.initColumnField(column, table);
                }
            }
        }
        List<OperateEnum.OperateType> allOperateType = OperateEnum.getAllOperateType();
        //创建工作薄对象
        HSSFWorkbook workbook=new HSSFWorkbook();
        //标题样式
        cellStyle_title = this.titleCellStyle(workbook);
        //内容样式
        cellStyle_content = this.contentCellStyle(workbook);
        
        //遍历表（多个表）
        for (GenTable genTable:
                tableList) {
            //主键信息
            GenTableColumn pkColumn = genTable.getPkColumn();
            //表描述
            String tableComment = genTable.getTableComment();
            //模块名
            String businessName = genTable.getBusinessName().toLowerCase();
            List<GenTableColumn> columnList = genTable.getColumns();
            //遍历操作类型（增，删，改，查，导入，导出）
            for (OperateEnum.OperateType operateType:
                allOperateType) {
                //操作类型
                String apiName = operateType.getApiName();
                //方法名
                String functionName = operateType.getFunctionName();
                //请求方式
                String methodType = operateType.getMethodType();
                //创建工作表对象
                sheet = workbook.createSheet(tableComment+apiName);
                sheet.setColumnWidth(0, 256*20+184);
                sheet.setColumnWidth(1, 256*20+184);
                sheet.setColumnWidth(2, 256*20+184);
                sheet.setColumnWidth(3, 256*20+184);
                sheet.setColumnWidth(4, 256*20+184);
                sheet.setColumnWidth(5, 256*20+184);
                //重置行下表
                rowIndex=0;
                //开始填充
                //请求描述
                row = sheet.createRow(rowIndex);
                row.setHeight((short) 300);
                cell = row.createCell(0);
                cell.setCellValue("请求描述：");
                cell.setCellStyle(cellStyle_title);
                cell = row.createCell(1);
                cell.setCellValue(tableComment+apiName);
                cell.setCellStyle(cellStyle_content);
                //填空白
                fillBlank(row,cell,cellStyle_content,2,4);
                //合并单元格
                sheet.addMergedRegion(new CellRangeAddress(0,0,1,4));
                //请求地址：
                rowIndex++;
                row =  sheet.createRow(rowIndex);
                row.setHeight((short) 300);
                cell = row.createCell(0);
                cell.setCellValue("请求地址：");
                cell.setCellStyle(cellStyle_title);
                cell = row.createCell(1);
                cell.setCellValue("http://ip:port/"+businessName+"/"+functionName);
                cell.setCellStyle(cellStyle_content);
                //填空白
                fillBlank(row,cell,cellStyle_content,2,4);
                //合并单元格
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,1,4));
                //请求方式：
                rowIndex++;
                row =  sheet.createRow(rowIndex);
                row.setHeight((short) 300);
                cell = row.createCell(0);
                cell.setCellValue("请求方式：");
                cell.setCellStyle(cellStyle_title);
                cell = row.createCell(1);
                cell.setCellValue(methodType);
                cell.setCellStyle(cellStyle_content);
                //填空白
                fillBlank(row,cell,cellStyle_content,2,4);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,1,4));
                //渲染单元格样式
            //        cell_0.setCellStyle(cellStyle_title);
            //        cell_1.setCellStyle(cellStyle_title);
                //空一行
                rowIndex++;
                row = sheet.createRow(rowIndex);
                row.setHeight((short) 300);
                cell = row.createCell(0);
                cell.setCellValue("");
                cell.setCellStyle(cellStyle_title);
                //填空白
                fillBlank(row,cell,cellStyle_content,0,4);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                //请求体
                rowIndex++;
                sheet.createRow(4).createCell(0).setCellValue("请求体：");
                row.setHeight((short) 300);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                //请求体表头：
                rowIndex++;
                row = sheet.createRow(rowIndex);
                row.setHeight((short) 300);
                row.createCell(0).setCellValue("名称");
                row.createCell(1).setCellValue("类型");
                row.createCell(2).setCellValue("必填");
                row.createCell(3).setCellValue("默认值");
                row.createCell(4).setCellValue("描述");
                StringBuilder requestDemo=new StringBuilder();
                switch(functionName){
                    case OperateConstants.LIST:
                        //请求体内容：遍历所有查询字段
                        for (GenTableColumn column:columnList) {
                            if (column.getIsQuery().equals("1")) {
                                rowIndex++;
                                row = sheet.createRow(rowIndex);
                                row.createCell(0).setCellValue(column.getJavaField().toLowerCase());
                                row.createCell(1).setCellValue(column.getJavaType().toLowerCase());
                                row.createCell(2).setCellValue("");
                                row.createCell(3).setCellValue("");
                                row.createCell(4).setCellValue(column.getColumnComment());
                                requestDemo.append(column.getJavaField().toLowerCase()+"=?&");
                            }
                        }
                        break;
                    case OperateConstants.ADD:
                        //请求体内容：遍历所有添加的字段
                        for (GenTableColumn column:columnList) {
                            if (column.getIsInsert().equals("1")) {
                                rowIndex++;
                                row = sheet.createRow(rowIndex);
                                row.createCell(0).setCellValue(column.getJavaField().toLowerCase());
                                row.createCell(1).setCellValue(column.getJavaType().toLowerCase());
                                row.createCell(2).setCellValue("");
                                row.createCell(3).setCellValue("");
                                row.createCell(4).setCellValue(column.getColumnComment());
                                //TODO
                                requestDemo.append(column.getJavaField().toLowerCase()+"=?&");
                            }
                        }
                        break;
                    case OperateConstants.EDIT:
                        //请求体内容：遍历所有修改的字段
                        for (GenTableColumn column:columnList) {
                            if (column.getIsEdit().equals("1")) {
                                rowIndex++;
                                row = sheet.createRow(rowIndex);
                                row.createCell(0).setCellValue(column.getJavaField().toLowerCase());
                                row.createCell(1).setCellValue(column.getJavaType().toLowerCase());
                                row.createCell(2).setCellValue("");
                                row.createCell(3).setCellValue("");
                                row.createCell(4).setCellValue(column.getColumnComment());
                                //TODO
                                requestDemo.append(column.getJavaField().toLowerCase()+"=?&");
                            }
                        }
                        break;
                    case OperateConstants.DELETE:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue(pkColumn.getJavaField().toLowerCase());
                        row.createCell(1).setCellValue(pkColumn.getJavaType().toLowerCase());
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue(pkColumn.getColumnComment());
                        requestDemo.append(pkColumn.getJavaField().toLowerCase()+"=?");
                        break;
                    case OperateConstants.DETAIL:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue(pkColumn.getJavaField().toLowerCase());
                        row.createCell(1).setCellValue(pkColumn.getJavaType().toLowerCase());
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue(pkColumn.getColumnComment());
                        requestDemo.append(pkColumn.getJavaField().toLowerCase()+"=?");
                        break;
                    case OperateConstants.IMPORT:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("file");
                        row.createCell(1).setCellValue("File");
                        row.createCell(2).setCellValue("true");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("导入文件");
                        break;
                    case OperateConstants.EXPORT:
                        //请求体内容：遍历所有查询字段
                        for (GenTableColumn column:columnList) {
                            if (column.getIsQuery().equals("1")) {
                                rowIndex++;
                                row = sheet.createRow(rowIndex);
                                row.createCell(0).setCellValue(column.getJavaField().toLowerCase());
                                row.createCell(1).setCellValue(column.getJavaType().toLowerCase());
                                row.createCell(2).setCellValue("");
                                row.createCell(3).setCellValue("");
                                row.createCell(4).setCellValue(column.getColumnComment());
                                requestDemo.append(column.getJavaField().toLowerCase()+"=?&");
                            }
                        }
                        break;
                    default :
                }
                //空一行
                rowIndex++;
                row = sheet.createRow(rowIndex);
                cell = row.createCell(0);
                cell.setCellValue("");
                cell.setCellStyle(cellStyle_title);
                //填空白
                fillBlank(row,cell,cellStyle_content,0,4);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                //请求体demo:
                rowIndex++;
                row = sheet.createRow(rowIndex);
                cell =row.createCell(0);
                cell.setCellValue("请求体demo:");
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,1,4));
                //请求体demo内容:
                rowIndex++;
                if (requestDemo.toString().endsWith("&")) {
                    requestDemo.deleteCharAt(requestDemo.lastIndexOf("&"));
                }
                row = sheet.createRow(rowIndex);
                cell =row.createCell(0);
                cell.setCellValue(requestDemo.toString());
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                //空一行
                rowIndex++;
                row = sheet.createRow(rowIndex);
                cell = row.createCell(0);
                cell.setCellValue("");
                cell.setCellStyle(cellStyle_title);
                //填空白
                fillBlank(row,cell,cellStyle_content,0,4);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                //响应参数(Body):
                rowIndex++;
                row = sheet.createRow(rowIndex);
                cell =row.createCell(0);
                cell.setCellValue("响应参数(Body):");
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,1,4));
                //响应参数(Body)表头：
                rowIndex++;
                row = sheet.createRow(rowIndex);
                row.createCell(0).setCellValue("名称");
                row.createCell(1).setCellValue("类型");
                row.createCell(2).setCellValue("必填");
                row.createCell(3).setCellValue("默认值");
                row.createCell(4).setCellValue("描述");
                //开始填充响应
                switch(functionName){
                    case OperateConstants.LIST:
                        List<PageColumnEnum.PageColumnType> allColumns = PageColumnEnum.getAllColumns();
                        //分页控件的字段列表
                        for (PageColumnEnum.PageColumnType pageColumnType:
                                allColumns) {
                            //字段名
                            String name = pageColumnType.getName();
                            //字段类型
                            String dataType = pageColumnType.getDataType();
                            //字段描述
                            String desc = pageColumnType.getDesc();
                            row = sheet.createRow(rowIndex);
                            row.createCell(0).setCellValue(name);
                            row.createCell(1).setCellValue(dataType);
                            row.createCell(2).setCellValue("");
                            row.createCell(3).setCellValue("");
                            row.createCell(4).setCellValue(desc);
                            rowIndex++;
                        }
                        //列表字段
                        for (GenTableColumn column:columnList) {
                            if (column.getIsQuery().equals("1")) {
                                row = sheet.createRow(rowIndex);
                                row.createCell(0).setCellValue(column.getJavaField().toLowerCase());
                                row.createCell(1).setCellValue(column.getJavaType().toLowerCase());
                                row.createCell(2).setCellValue("");
                                row.createCell(3).setCellValue("");
                                row.createCell(4).setCellValue(column.getColumnComment());
                                rowIndex++;
                            }
                        }
                        break;
                    case OperateConstants.ADD:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("code");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("错误码");
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("msg");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("错误信息");
                        break;
                    case OperateConstants.EDIT:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("code");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("错误码");
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("msg");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("错误信息");
                        break;
                    case OperateConstants.DELETE:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("code");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("错误码");
                        rowIndex++;
                        HSSFRow row_responseBody_msg_delete = sheet.createRow( rowIndex);
                        row_responseBody_msg_delete.createCell(0).setCellValue("msg");
                        row_responseBody_msg_delete.createCell(1).setCellValue("");
                        row_responseBody_msg_delete.createCell(2).setCellValue("");
                        row_responseBody_msg_delete.createCell(3).setCellValue("");
                        row_responseBody_msg_delete.createCell(4).setCellValue("错误信息");
                        break;
                    case OperateConstants.DETAIL:
                        //列表字段
                        for (GenTableColumn column:columnList) {
                            if (column.getIsQuery().equals("1")) {
                                rowIndex++;
                                row = sheet.createRow(rowIndex);
                                row.createCell(0).setCellValue(column.getJavaField().toLowerCase());
                                row.createCell(1).setCellValue(column.getJavaType().toLowerCase());
                                row.createCell(2).setCellValue("");
                                row.createCell(3).setCellValue("");
                                row.createCell(4).setCellValue(column.getColumnComment());
                            }
                        }
                        break;
                    case OperateConstants.IMPORT:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("code");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("错误码");
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("msg");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("错误信息");
                        break;
                    case OperateConstants.EXPORT:
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue("");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("");
                        row = sheet.createRow(rowIndex);
                        rowIndex++;
                        row.createCell(0).setCellValue("");
                        row.createCell(1).setCellValue("");
                        row.createCell(2).setCellValue("");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("");
                        break;
                    default :
                }
                //空一行
                rowIndex++;
                row = sheet.createRow(rowIndex);
                cell = row.createCell(0);
                cell.setCellValue("");
                cell.setCellStyle(cellStyle_title);
                //填空白
                fillBlank(row,cell,cellStyle_content,0,4);
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                //响应参数(Body)demo
                rowIndex++;
                row = sheet.createRow(rowIndex);
                cell = row.createCell(0);
                cell.setCellValue("响应参数demo");
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,1,4));
                switch(functionName){
                    case OperateConstants.LIST:
                        break;
                    case OperateConstants.ADD:
                        //响应参数(Body)demo
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        cell =row.createCell(0);
                        cell.setCellValue("\n" +
                                "{\n" +
                                "  \"code\": 0,\n" +
                                "  \"msg\": \"操作成功\"\n" +
                                "}");
                        cell.setCellStyle(cellStyle_content);
                        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                        break;
                    case OperateConstants.EDIT:
                        //响应参数(Body)demo
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        cell =row.createCell(0);
                        cell.setCellValue("\n" +
                                "{\n" +
                                "  \"code\": 0,\n" +
                                "  \"msg\": \"操作成功\"\n" +
                                "}");
                        cell.setCellStyle(cellStyle_content);
                        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                        break;
                    case OperateConstants.DELETE:
                        //响应参数(Body)demo
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        cell =row.createCell(0);
                        cell.setCellValue("\n" +
                                "{\n" +
                                "  \"code\": 0,\n" +
                                "  \"msg\": \"操作成功\"\n" +
                                "}");
                        cell.setCellStyle(cellStyle_content);
                        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                        break;
                    case OperateConstants.DETAIL:
                        break;
                    case OperateConstants.IMPORT:
                        //响应参数(Body)demo
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        cell =row.createCell(0);
                        cell.setCellValue("\n" +
                                "{\n" +
                                "  \"code\": 0,\n" +
                                "  \"msg\": \"操作成功\"\n" +
                                "}");
                        cell.setCellStyle(cellStyle_content);
                        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                        break;
                    case OperateConstants.EXPORT:
                        //响应参数(Body)demo
                        rowIndex++;
                        row = sheet.createRow(rowIndex);
                        cell =row.createCell(0);
                        cell.setCellValue("");
                        cell.setCellStyle(cellStyle_content);
                        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
                        break;
                    default :
                }
            }
        }
        return workbook;
    }
    //设置表头样式
    public HSSFCellStyle titleCellStyle(HSSFWorkbook workbook){
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //右边框
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //左边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //下边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //上边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //背景色
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font dataFont = workbook.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        dataFont.setBold(true);
        cellStyle.setFont(dataFont);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        return cellStyle;
    }
    //设置内容样式
    public HSSFCellStyle contentCellStyle(HSSFWorkbook workbook){
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        //设置水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //右边框
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //左边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //下边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //上边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        //设置字体
        HSSFFont font = workbook.createFont();
//        font.setFontName("华文行楷");//设置字体名称
        font.setFontHeightInPoints((short)8);//设置字号
        font.setItalic(false);//设置是否为斜体
//        font.setBold(true);//设置是否加粗
//        font.setColor(IndexedColors.RED.index);//设置字体颜色
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        //设置背景
//        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        return cellStyle;
    }
    //解析excel
    public List<GenTable> readExcel(MultipartFile file){
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //实例化实现了AnalysisEventListener接口的类
        ExcelListener listener = new ExcelListener();
        //传入参数
        ExcelReader excelReader = new ExcelReader(inputStream, ExcelTypeEnum.XLSX, null, listener);
        //sheet的个数
        int sheetSize = excelReader.getSheets().size();
        List<GenTable> tableList=new ArrayList<GenTable>();
        //从第三个sheet开始读
        for (int i = 3; i <= sheetSize ; i++) {
            //每次读数据前先清空数据
            listener.clearData();
            ArrayList<GenTableColumn> columnList = new ArrayList<>();
            //读取信息
            excelReader.read(new com.alibaba.excel.metadata.Sheet(i));
            //获取数据
            List<Object> list = listener.getDatas();
            if (list == null) {
                continue;
            }
            Object ob = (Object) list;
            List<List<String>> listRole = (List<List<String>>) ob;
            //解析第二行
            List<String> tableNameRow = listRole.get(1);
            //解析第三行
            List<String> tableCommentRow = listRole.get(2);
            //解析第三行
            List<String> tableModuleNameRow = listRole.get(3);
            //表名
            String tableName = tableNameRow.get(1).toUpperCase();
            //表描述
            String tableComment = tableCommentRow.get(1).toUpperCase();
            //表模块名
            String tableModuleName = tableModuleNameRow.get(1).toUpperCase();
            GenTable genTable = new GenTable();
            genTable.setTableName(tableName);
            genTable.setTableComment(tableComment);
            genTable.setBusinessName(tableModuleName);
            //从第六行开始解析列信息
            for (int j = 5; j < listRole.size(); j++) {
                List<String> columnRow = listRole.get(j);
                String columnName = columnRow.get(0) == null ? "" : columnRow.get(0).toUpperCase();
                String columnComment = columnRow.get(1) == null ? "" : columnRow.get(1).toUpperCase();
                String columnType = columnRow.get(2) == null ? "" : columnRow.get(2).toUpperCase();
                String isPk = columnRow.get(3) == null ? "" : columnRow.get(3).toUpperCase();
                String memo = columnRow.get(4) == null ? "" : columnRow.get(4).toUpperCase();
                if (!StringUtils.isEmpty(memo)) {
                    columnComment = columnComment + ":" + memo;
                }
                GenTableColumn genTableColumn = new GenTableColumn();
                genTableColumn.setColumnName(columnName);
                genTableColumn.setColumnComment(columnComment);
                genTableColumn.setColumnType(columnType);
                genTableColumn.setIsPk(isPk);
                columnList.add(genTableColumn);
            }
            genTable.setColumns(columnList);
            tableList.add(genTable);
        }
        return tableList;
    }
    //填空白
    public static void fillBlank( HSSFRow row,HSSFCell cell,CellStyle cellStyle_content,int startColumn,int endColumn){
        for (int i = startColumn; i <= endColumn; i++) {
            cell = row.createCell(i);
            cell.setCellValue("");
            cell.setCellStyle(cellStyle_content);
        }
    }
    //填充一行请求参数
    /**
     * @Author Jumy
     * @Description
     * @Date
     * @Param   HSSFSheet sheet
     * @Param   HSSFRow row
     * @Param   HSSFCell cell
     * @Param   CellStyle cellStyle_content
     * @Param   int rowIndex
     * @Param   GenTableColumn column
     * @return
     **/
    public static void fillOneRow_type1(HSSFSheet sheet, HSSFRow row, HSSFCell cell, CellStyle cellStyle_content,  int rowIndex, GenTableColumn column) {
        row = sheet.createRow(rowIndex);
        cell= row.createCell(0);
        cell.setCellValue(column.getJavaField().toLowerCase());
        cell.setCellStyle(cellStyle_content);
        row.createCell(1);
        cell.setCellValue(column.getJavaType().toLowerCase());
        cell.setCellStyle(cellStyle_content);
        row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(4);
        cell.setCellValue(column.getColumnComment());
        cell.setCellStyle(cellStyle_content);
    }
    //填充一行空白
    /**
     * @Author Jumy
     * @Description
     * @Date
     * @Param   HSSFSheet sheet
     * @Param   HSSFRow row
     * @Param   HSSFCell cell
     * @Param   CellStyle cellStyle_content
     * @Param   int rowIndex
     * @Param   GenTableColumn column
     * @return
     **/
    public static void fillOneRow_head(HSSFSheet sheet, HSSFRow row, HSSFCell cell, CellStyle cellStyle_content,  int rowIndex) {
        rowIndex++;
        row = sheet.createRow(rowIndex);
        cell= row.createCell(0);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_title);
        row.createCell(1);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(4);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,1,4));
    }
    //填充一行空白
    /**
     * @Author Jumy
     * @Description
     * @Date
     * @Param   HSSFSheet sheet
     * @Param   HSSFRow row
     * @Param   HSSFCell cell
     * @Param   CellStyle cellStyle_content
     * @Param   int rowIndex
     * @Param   GenTableColumn column
     * @return
     **/
    public static void fillOneRow_blank(HSSFSheet sheet, HSSFRow row, HSSFCell cell, int rowIndex) {
        rowIndex++;
        row = sheet.createRow(rowIndex);
        cell= row.createCell(0);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(1);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        row.createCell(4);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,0,4));
    }
    //填充一行请求参数(表头)
    /**
     * @Author Jumy
     * @Description
     * @Date
     * @Param   HSSFSheet sheet
     * @Param   HSSFRow row
     * @Param   HSSFCell cell
     * @Param   CellStyle cellStyle_content
     * @Param   int rowIndex
     * @Param   GenTableColumn column
     * @return
     **/
    public static void fillOneRow_title(int rowIndex) {
        rowIndex++;
        row = sheet.createRow(rowIndex);
        cell= row.createCell(0);
        cell.setCellValue("名称");
        cell.setCellStyle(cellStyle_title);
        row.createCell(1);
        cell.setCellValue("类型");
        cell.setCellStyle(cellStyle_title);
        row.createCell(2);
        cell.setCellValue("必填");
        cell.setCellStyle(cellStyle_title);
        row.createCell(3);
        cell.setCellValue("默认值");
        cell.setCellStyle(cellStyle_title);
        row.createCell(4);
        cell.setCellValue("描述");
        cell.setCellStyle(cellStyle_title);
    }
}
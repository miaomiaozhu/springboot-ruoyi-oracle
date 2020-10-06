package com.ruoyi.generator.service.impl;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.ruoyi.common.json.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.generator.config.OperateConstants;
import com.ruoyi.generator.config.OperateEnum;
import com.ruoyi.generator.config.PageColumnEnum;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;
import com.ruoyi.generator.easyexcel.ExcelListener;
import com.ruoyi.generator.service.IApiDocService;
import com.ruoyi.generator.sql.SqlConstants;
import com.ruoyi.generator.util.GenUtils;
import org.apache.commons.collections.list.SynchronizedList;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 业务 服务层实现
 *
 * @author ruoyi
 */
@Service
public class ApiDocServiceImpl implements IApiDocService {
    private static final Logger log = LoggerFactory.getLogger(ApiDocServiceImpl.class);
    //header样式
    public static HSSFCellStyle cellStyle_header;
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

    @Override
    public HSSFWorkbook generateApiDoc(MultipartFile file) {
        List<GenTable> tableList = readExcel(file);
        //初始化列信息
        for (GenTable table : tableList) {
            {
                for (GenTableColumn column : table.getColumns()) {
                    //提取数据库数据类型
                    String columnType = column.getColumnType().toLowerCase();
                    if (columnType.contains("(")) {
                        columnType = columnType.substring(0, columnType.lastIndexOf("("));
                    }
                    column.setColumnType(columnType);
                    if (!StringUtils.isEmpty(column.getIsPk()) && SqlConstants.IS_PK_SCOPE.contains(column.getIsPk())) {
                        table.setPkColumn(column);
                    }
                    GenUtils.initColumnField(column, table);
                }
            }
        }
        List<OperateEnum.OperateType> allOperateType = OperateEnum.getAllOperateType();
        //创建工作薄对象
        HSSFWorkbook workbook = new HSSFWorkbook();
        //header样式
        cellStyle_header = this.headerCellStyle(workbook);
        //标题样式
        cellStyle_title = this.titleCellStyle(workbook);
        //内容样式
        cellStyle_content = this.contentCellStyle(workbook);

        //遍历表（多个表）
        for (GenTable genTable :
                tableList) {
            //主键信息
            GenTableColumn pkColumn = genTable.getPkColumn();
            //表描述
            String tableComment = genTable.getTableComment();
            //模块名
            String businessName = genTable.getBusinessName().toLowerCase();
            //表的所有列
            List<GenTableColumn> columnList = genTable.getColumns();
            //可查询的所有列
            List<GenTableColumn> columnList_list = getColumnsForOperate(columnList, "list");
            //可新增的所有列
            List<GenTableColumn> columnList_add = getColumnsForOperate(columnList, "add");
            //可修改的所有列
            List<GenTableColumn> columnList_edit = getColumnsForOperate(columnList, "edit");

            //遍历操作类型（增，删，改，查，导入，导出）
            for (OperateEnum.OperateType operateType :
                    allOperateType) {
                //操作类型
                String apiName = operateType.getApiName();
                //方法名
                String functionName = operateType.getFunctionName();
                //请求方式
                String methodType = operateType.getMethodType();
                //创建工作表对象
                sheet = workbook.createSheet(tableComment + apiName);
                //设置列宽
                setColumnWidth();
                //重置行下表
                rowIndex = 0;
                //开始填充excel
                fillHeader(tableComment + apiName);
                //请求描述
                fillOneRow_keyValue("请求描述：", tableComment + apiName);
                //请求地址：
                fillOneRow_keyValue("请求地址：", businessName + "/" + functionName);
                //请求方式：
                fillOneRow_keyValue("请求方式：", methodType);
                //空一行
                fillOneRow_blank();
                //请求体
                fillOneRow_keyValue("请求体：", "");
                //请求体表头：
                fillOneRow_title();
                //请求参数
                //请求json
                String requestJson = "";
                switch (functionName) {
                    case OperateConstants.LIST:
                        //请求体内容：遍历所有查询字段
                        for (GenTableColumn column : columnList_list) {
                            //填充请求参数
                            fillOneRow_body(column);
                            requestJson+=column.getJavaField().toLowerCase() + "=?&";
                        }
                        break;
                    case OperateConstants.ADD:
                        //请求体内容：遍历所有添加的字段
                        for (GenTableColumn column : columnList_add) {
                            //填充请求参数
                            fillOneRow_body(column);
                        }
                        requestJson = generateRequestJson(columnList_add);
                        break;
                    case OperateConstants.EDIT:
                        //请求体内容：遍历所有修改的字段
                        for (GenTableColumn column : columnList_edit) {
                            //填充请求参数
                            fillOneRow_body(column);
                        }
                        requestJson = generateRequestJson(columnList_edit);
                        break;
                    case OperateConstants.DELETE:
                        //填充请求参数
                        fillOneRow_body(pkColumn);
                        requestJson+=pkColumn.getJavaField().toLowerCase() + "=?&";
                        break;
                    case OperateConstants.DETAIL:
                        //填充请求参数
                        fillOneRow_body(pkColumn);
                        requestJson+=pkColumn.getJavaField().toLowerCase() + "=?&";
                        break;
                    case OperateConstants.IMPORT:
                        //填充请求参数
                        GenTableColumn column_file = new GenTableColumn();
                        column_file.setJavaField("file");
                        column_file.setJavaType("File");
                        column_file.setColumnComment("导入文件");
                        //填充请求参数
                        fillOneRow_body(column_file);
                        break;
                    case OperateConstants.EXPORT:
                        //请求体内容：遍历所有查询字段
                        for (GenTableColumn column : columnList_list) {
                            //填充请求参数
                            fillOneRow_body(column);
                            requestJson+=pkColumn.getJavaField().toLowerCase() + "=?&";
                        }
                        break;
                    default:
                }
                //空一行
                fillOneRow_blank();
                //请求体demo:
                fillOneRow_keyValue("请求体demo：", "");
                //请求体demo内容:
                if (requestJson.endsWith("&")) {
                    requestJson=requestJson.substring(0,requestJson.length()-1);
                }
                //请求体demo:
                fillOneRow_merge(requestJson);
                //空一行
                fillOneRow_blank();
                //响应参数(Body):
                fillOneRow_keyValue("响应参数(Body):", "");
                //响应参数(Body)表头：
                fillOneRow_title();
                //开始填充响应
                switch (functionName) {
                    case OperateConstants.LIST:
                        List<PageColumnEnum.PageColumnType> allColumns = PageColumnEnum.getAllColumns();
                        //分页控件的字段列表
                        for (PageColumnEnum.PageColumnType pageColumnType :
                                allColumns) {
                            //字段名
                            String name = pageColumnType.getName();
                            //字段类型
                            String dataType = pageColumnType.getDataType();
                            //字段描述
                            String desc = pageColumnType.getDesc();
                            GenTableColumn column_response = new GenTableColumn();
                            column_response.setJavaField(name);
                            column_response.setJavaType(dataType);
                            column_response.setColumnComment(desc);
                            //填充一行
                            fillOneRow_body(column_response);
                        }
                        GenTableColumn column_list = new GenTableColumn();
                        column_list.setJavaField("list");
                        column_list.setJavaType("object");
                        column_list.setColumnComment("数据");
                        //填充一行
                        fillOneRow_body(column_list);
                        //列表字段
                        ArrayList<GenTableColumn> columns_temp = new ArrayList<>();
                        for (GenTableColumn column : columnList_list) {
                            GenTableColumn column_new = new GenTableColumn();
                            BeanUtils.copyBeanProp(column_new,column);
                            columns_temp.add(column_new);
                        }
                        for (GenTableColumn column : columns_temp) {
                            column.setJavaField("    "+column.getJavaField());
                            //填充一行
                            fillOneRow_body(column);
                        }
                        break;
                    case OperateConstants.ADD:
                        //填充通用POST操作的响应体
                        fillCodeMsg();
                        break;
                    case OperateConstants.EDIT:
                        //填充通用POST操作的响应体
                        fillCodeMsg();
                        break;
                    case OperateConstants.DELETE:
                        //填充通用POST操作的响应体
                        fillCodeMsg();
                        break;
                    case OperateConstants.DETAIL:
                        //列表字段
                        for (GenTableColumn column : columnList_list) {
                            //填充一行
                            fillOneRow_body(column);
                        }
                        break;
                    case OperateConstants.IMPORT:
                        //填充通用POST操作的响应体
                        fillCodeMsg();
                        break;
                    case OperateConstants.EXPORT:
                        GenTableColumn column = new GenTableColumn();
                        column.setJavaField("");
                        column.setJavaType("");
                        column.setColumnComment("");
                        //填充一行
                        fillOneRow_body(column);
                        break;
                    default:
                }
                //空一行
                fillOneRow_blank();
                //响应参数(Body)demo
                fillOneRow_keyValue("响应参数(Body)demo", "");
                String responseJson = "";
                switch (functionName) {
                    case OperateConstants.LIST:
                        //列表字段
                        responseJson = generateResponseJsonForPage(columnList_list);
                        fillOneRow_merge(responseJson);
                        break;
                    case OperateConstants.ADD:
                        //响应参数(Body)demo
                        fillCodeMsgContent();
                        break;
                    case OperateConstants.EDIT:
                        //响应参数(Body)demo
                        fillCodeMsgContent();
                        break;
                    case OperateConstants.DELETE:
                        fillCodeMsgContent();
                        break;
                    case OperateConstants.DETAIL:
                        //请求体内容：遍历所有查询字段
                        for (GenTableColumn column : columnList_list) {
                            //填充请求参数
                            fillOneRow_body(column);
                        }
                        responseJson = generateResponseJson(columnList_list);
                        fillOneRow_merge(responseJson);
                        break;
                    case OperateConstants.IMPORT:
                        fillCodeMsgContent();
                        break;
                    case OperateConstants.EXPORT:
                        //响应参数(Body)demo
                        fillOneRow_merge("");
                        break;
                    default:
                }
                //响应一栏高度设置高一点
                row.setHeight((short) 1000);
            }
        }
        return workbook;
    }

    //设置header样式
    public HSSFCellStyle headerCellStyle(HSSFWorkbook workbook) {
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
        HSSFColor lightGreen = setColor(workbook, (byte) 155, (byte) 187, (byte) 89);
        cellStyle.setFillForegroundColor((lightGreen.getIndex()));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font dataFont = workbook.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 14);
        dataFont.setBold(true);
        cellStyle.setFont(dataFont);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    //设置表头样式
    public HSSFCellStyle titleCellStyle(HSSFWorkbook workbook) {
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
        HSSFColor lightGreen = setColor(workbook, (byte) 155, (byte) 187, (byte) 89);
        cellStyle.setFillForegroundColor((lightGreen.getIndex()));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font dataFont = workbook.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 12);
        dataFont.setBold(true);
        cellStyle.setFont(dataFont);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        return cellStyle;
    }

    //设置内容样式
    public HSSFCellStyle contentCellStyle(HSSFWorkbook workbook) {
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
        font.setFontHeightInPoints((short) 10);//设置字号
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
    public List<GenTable> readExcel(MultipartFile file) {
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
        List<GenTable> tableList = new ArrayList<GenTable>();
        //从第三个sheet开始读
        for (int i = 3; i <= sheetSize; i++) {
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

    //设置行高
    public static void commonForRow() {
        rowIndex++;
        row.setHeight((short) 330);
    }

    //填充一行(key_value)
    public static void fillOneRow_keyValue(String key, String value) {
        commonForRow();
        row = sheet.createRow(rowIndex);
        cell = row.createCell(0);
        cell.setCellValue(key);
        cell.setCellStyle(cellStyle_title);
        cell = row.createCell(1);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(4);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 4));
    }

    //填充一行(head)
    public static void fillOneRow_head(String headName) {
        commonForRow();
        row.setHeight((short) 300);
        row = sheet.createRow(rowIndex);
        cell = row.createCell(0);
        cell.setCellValue(headName);
        cell.setCellStyle(cellStyle_title);
        cell = row.createCell(1);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(4);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 4));
    }

    //填充一行(空白)
    public static void fillOneRow_blank() {
        commonForRow();
        row = sheet.createRow(rowIndex);
        cell = row.createCell(0);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(1);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(4);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 4));
    }

    //填充一行(合并单元格)
    public static void fillOneRow_merge(String content) {
        commonForRow();
        row = sheet.createRow(rowIndex);
        cell = row.createCell(0);
        cell.setCellValue(content);
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(1);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(4);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 4));
    }

    //填充一行请求参数(表头)
    public static void fillOneRow_title() {
        commonForRow();
        row = sheet.createRow(rowIndex);
        cell = row.createCell(0);
        cell.setCellValue("名称");
        cell.setCellStyle(cellStyle_title);
        cell = row.createCell(1);
        cell.setCellValue("类型");
        cell.setCellStyle(cellStyle_title);
        cell = row.createCell(2);
        cell.setCellValue("必填");
        cell.setCellStyle(cellStyle_title);
        cell = row.createCell(3);
        cell.setCellValue("默认值");
        cell.setCellStyle(cellStyle_title);
        cell = row.createCell(4);
        cell.setCellValue("描述");
        cell.setCellStyle(cellStyle_title);
    }

    //填充一行请求参数(表内容)
    public static void fillOneRow_body(GenTableColumn column) {
        commonForRow();
        row = sheet.createRow(rowIndex);
        cell = row.createCell(0);
        cell.setCellValue(column.getJavaField().toLowerCase());
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(1);
        cell.setCellValue(column.getJavaType().toLowerCase());
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_content);
        cell = row.createCell(4);
        cell.setCellValue(column.getColumnComment());
        cell.setCellStyle(cellStyle_content);
    }

    //填充通用POST操作的响应体
    public void fillCodeMsg() {
        GenTableColumn column = new GenTableColumn();
        column.setJavaField("code");
        column.setJavaType("");
        column.setColumnComment("错误码");
        //填充一行
        fillOneRow_body(column);
        column.setJavaField("msg");
        column.setJavaType("");
        column.setColumnComment("错误信息");
        //填充一行
        fillOneRow_body(column);
    }

    //填充通用POST操作的响应体内容
    public void fillCodeMsgContent() {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("msg", "操作成功");
        fillOneRow_merge(result.toString());
    }

    //设置列宽
    public void setColumnWidth() {
        sheet.setColumnWidth(0, 256 * 20 + 184);
        sheet.setColumnWidth(1, 256 * 20 + 184);
        sheet.setColumnWidth(2, 256 * 20 + 184);
        sheet.setColumnWidth(3, 256 * 20 + 184);
        sheet.setColumnWidth(4, 256 * 40 + 184);
    }

    //填充表头
    public void fillHeader(String title) {
        row = sheet.createRow(0);
        cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(cellStyle_header);
        cell = row.createCell(1);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_header);
        cell = row.createCell(2);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_header);
        cell = row.createCell(3);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_header);
        cell = row.createCell(4);
        cell.setCellValue("");
        cell.setCellStyle(cellStyle_header);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 4));
    }

    public HSSFColor setColor(HSSFWorkbook workbook, byte r, byte g, byte b) {
        HSSFPalette palette = workbook.getCustomPalette();
        HSSFColor hssfColor = null;
        try {
            hssfColor = palette.findColor(r, g, b);
            if (hssfColor == null) {
                palette.setColorAtIndex(HSSFColor.LAVENDER.index, r, g, b);
                hssfColor = palette.getColor(HSSFColor.LAVENDER.index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hssfColor;
    }

    //组装请求json
    public String generateRequestJson(List<GenTableColumn> columnList) {
        JSONObject result = new JSONObject();
        for (GenTableColumn column :
                columnList) {
            result.put(column.getJavaField(), null);
        }
        return result.toString();
    }

    //组装响应json(分页数据)
    public String generateResponseJsonForPage(List<GenTableColumn> columnList) {
        JSONObject result = new JSONObject();
        List<PageColumnEnum.PageColumnType> allColumns = PageColumnEnum.getAllColumns();
        for (PageColumnEnum.PageColumnType pageColumnType :
                allColumns) {
            //字段名
            String name = pageColumnType.getName();
            //字段类型
            String dataType = pageColumnType.getDataType();
            result.put(pageColumnType.getName(), null);
        }
        JSONArray data = new JSONArray();
        for (int i = 0; i < 3; i++) {
            JSONObject object = new JSONObject();
            for (GenTableColumn column :
                    columnList) {
                object.put(column.getJavaField(), null);
            }
            data.add(object);
        }

        result.put("list", data);
        return result.toString();
    }

    //组装响应json
    public String generateResponseJson(List<GenTableColumn> columnList) {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("msg", "操作成功");
        JSONObject data = new JSONObject();
        for (GenTableColumn column :
                columnList) {
            data.put(column.getJavaField(), null);
        }
        result.put("data", data);
        return result.toString();
    }

    //获得可查询，可添加，可修改的列
    public List<GenTableColumn> getColumnsForOperate(List<GenTableColumn> columnList, String operateType) {
        List<GenTableColumn> columns = Collections.synchronizedList(new ArrayList<>());
        switch (operateType) {
            case "list":
                for (GenTableColumn column : columnList) {
                    if (column.getIsQuery().equals("1")) {
                        columns.add(column);
                    }
                }
                break;
            case "add":
                for (GenTableColumn column : columnList) {
                    if (column.getIsInsert().equals("1")) {
                        columns.add(column);
                    }
                }
                break;
            case "edit":
                for (GenTableColumn column : columnList) {
                    if (column.getIsEdit().equals("1")) {
                        columns.add(column);
                    }
                }
                break;
            default:
                break;

        }
        return columns;
    }
}
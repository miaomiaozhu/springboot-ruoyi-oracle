package com.ruoyi.generator.controller;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.generator.easyexcel.ExcelListener;
import com.ruoyi.generator.service.ISqlService;
import com.ruoyi.generator.vo.ExcelColumn;
import com.ruoyi.generator.vo.ExcelTable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SQLController
 * @Author Jumy
 * @Description //TODO
 * @Date 2020/10/5 13:24
 * @Version 1.0
 **/
@Api(description = "sql语句生成管理")
@Controller
@RequestMapping("/tool/sql")
public class SqlController extends BaseController {
    @Autowired
    private ISqlService sqlService;
    /**
     * test
     *
     * */
    @ApiOperation(value="test",notes="test")
//    @RequiresPermissions("tool:gen:sql")
//    @Log(title = "批量生成sql", businessType = BusinessType.GENCODE)
    @PostMapping("/test")
    public void test(@ApiParam(value="需要导入的excel文档") @RequestParam MultipartFile file) throws IOException {
    }
    /**
     * excel批量生成sql
     *
     * */
    @ApiOperation(value="excel批量生成sql",notes="excel批量生成sql")
//    @RequiresPermissions("tool:gen:sql")
//    @Log(title = "批量生成sql", businessType = BusinessType.GENCODE)
    @PostMapping("/batchGenSql")
    @ResponseBody
    public String batchGenSql(@ApiParam(value="需要导入的excel文档") @RequestParam MultipartFile file) throws IOException
    {
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
        //整体sql
        StringBuilder sql = new StringBuilder();
        //从第三个sheet开始读
        for (int i = 3; i <= sheetSize ; i++) {
            //每次读数据前先清空数据
            listener.clearData();
            ArrayList<ExcelColumn> columnList = new ArrayList<>();
            //读取信息
//            excelReader.read(new Sheet(i+1, 5, ExcelColumn.class));
            excelReader.read(new Sheet(i));
            //获取数据
            List<Object> list = listener.getDatas();
            if (list == null) {
                continue;
            }
            Object ob = (Object) list;
            List<List<String>> listRole = (List<List<String>>)ob ;
            //解析第二行
            List<String> tableNameRow = listRole.get(1);
            //解析第三行
            List<String> tableCommentRow = listRole.get(2);
            //解析第四行
            List<String> tableModuleNameRow = listRole.get(3);
            //表名
            String tableName = tableNameRow.get(1).toUpperCase();
            //表描述
            String tableComment = tableCommentRow.get(1).toUpperCase();
            ExcelTable excelTable = new ExcelTable(tableName, tableComment);
            //从第六行开始解析列信息
            for (int j = 5; j < listRole.size(); j++) {
                List<String> columnRow = listRole.get(j);
                String columnName = columnRow.get(0)==null? "":columnRow.get(0).toUpperCase();
                String columnComment =columnRow.get(1)==null? "": columnRow.get(1).toUpperCase();
                String columnType = columnRow.get(2)==null? "":columnRow.get(2).toUpperCase();
                String isPk = columnRow.get(3)==null?"":columnRow.get(3).toUpperCase();
                String memo = columnRow.get(4)==null?"":columnRow.get(4).toUpperCase();
                if (!StringUtils.isEmpty(memo)) {
                    columnComment=columnComment+":"+memo;
                }
                ExcelColumn excelColumn = new ExcelColumn( columnName, columnComment, columnType, isPk);
                columnList.add(excelColumn);
            }
            //单表的sql
            String table_sql = sqlService.generateSql(excelTable,columnList);
            //将单表的sql插入到整体的sql中
            if (i >3) {
                sql.append("\n");
            }
            sql.append(table_sql);
        }
        return sql.toString();
    }
}
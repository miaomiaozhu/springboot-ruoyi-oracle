package com.ruoyi.generator.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.generator.service.IApiDocService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @ClassName SQLController
 * @Author Jumy
 * @Description //TODO
 * @Date 2020/10/5 13:24
 * @Version 1.0
 **/
@Api(description = "api接口文档生成管理")
@Controller
@RequestMapping("/tool/doc")
public class ApiDocController extends BaseController {
    @Autowired
    private IApiDocService apiDocService;
    /**
     * 生成接口文档
     *
     * */
//    @RequiresPermissions("tool:gen:apidoc")
//    @Log(title = "生成接口文档", businessType = BusinessType.GENCODE)
    @ApiOperation(value="生成接口文档",notes="生成接口文档")
    @PostMapping("/generateApiDoc")
    public void generateApiDoc(@ApiParam(value="需要导入的excel文档") @RequestParam MultipartFile file,HttpServletResponse response) throws IOException
    {
        HSSFWorkbook workbook = apiDocService.generateApiDoc(file);
        OutputStream outputStream = response.getOutputStream();
        try {
            String filename= DateUtils.dateTimeNow()+"_api.xls";
            //添加响应头信息
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            //设置类型
            response.setContentType("application/msexcel;charset=UTF-8");
            //设置头
            response.setHeader("Pragma", "No-cache");
            //设置头
            response.setHeader("Cache-Control", "no-cache");
            //设置日期头
            response.setDateHeader("Expires", 0);
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
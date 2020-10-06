package com.ruoyi.generator.service;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

/**
 * 业务 服务层
 *
 * @author ruoyi
 */
public interface IApiDocService {
    /**
     * 生成api文档
     *
     * @param file
     * @return
     */
    public HSSFWorkbook generateApiDoc(MultipartFile file);

}

package com.ruoyi.generator.config;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

public enum OperateEnum {
    LIST("查询列表","list","GET"),
    ADD("新增", "add","POST"),
    EDIT("修改", "edit","POST"),
    DELETE("删除", "delete","POST"),
    DETAIL("查询详情", "detail","GET"),
    IMPORT("导入", "import","POST"),
    EXPPORT("导出", "export","GET");

    private final String apiName;
    private final String functionName;
    private final String methodType;

    public String getApiName() {
        return apiName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getMethodType() {
        return methodType;
    }

    OperateEnum(String apiName, String functionName, String methodType) {
        this.apiName = apiName;
        this.functionName = functionName;
        this.methodType = methodType;
    }
    public static List<OperateType> getAllOperateType() {
        //获取枚举值转list集合
        //这个model是自定义的一个类  放了两个字段，一个枚举值  一个枚举名称
        List<OperateType> list = new LinkedList<>();
        for (OperateEnum operateEnum : values()) {
            OperateType operateType = new OperateType();
            operateType.setApiName(operateEnum.getApiName());
            operateType.setFunctionName(operateEnum.getFunctionName());
            operateType.setMethodType(operateEnum.getMethodType());
            list.add(operateType);
        }
        return list;
    }



    /**
     * 对象
     */
    @Data
    public static class OperateType {
        public String apiName;
        public String functionName;
        public String methodType;

    }

}

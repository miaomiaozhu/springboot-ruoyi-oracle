package com.ruoyi.generator.config;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

public enum PageColumnEnum {
    PAGENUM("pageNum","当前页页数","string"),
    PAGESIZE("pageSize","一页数据条数","string"),
    SIZE("size","总数据条数","string"),
    STARTROW("startRow","开始行","string"),
    ENDROW("endRow","结束行","string"),
    TOTAL("total","总数据条数","string"),
    PAGES("pages","总页数","string"),
    FIRSTPAGE("firstPage","第一页页数","string"),
    PREPAGE("prePage","上一页页数","string"),
    NEXTPAGE("nextPage","下一页页数","string"),
    LASTPAGE("lastPage","最后一页页数","string"),
    ISFIRSTPAGE("isFirstPage","是否是第一页","string"),
    ISLASTPAGE("isLastPage","是否是最后一页","string"),
    HASPREVIOUSPAGE("hasPreviousPage","是否有上一页","string"),
    HASNEXTPAGE("hasNextPage","是否有下一页","string"),
    NAVIGATEFIRSTPAGE("navigateFirstPage","","number"),
    NAVIGATELASTPAGE("navigateLastPage","","number"),
    NAVIGATEPAGES("navigatePages","","string"),
    NAVIGATEPAGENUMS("navigatepageNums","","string");

    private final String name;
    private final String desc;
    private final String dataType;

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getDataType() {
        return dataType;
    }

    PageColumnEnum(String name, String desc, String dataType) {

        this.name = name;
        this.desc = desc;
        this.dataType = dataType;
    }

    public static List<PageColumnType> getAllColumns() {
        //获取枚举值转list集合
        //这个model是自定义的一个类  放了两个字段，一个枚举值  一个枚举名称
        List<PageColumnType> list = new LinkedList<>();
        for (PageColumnEnum operateEnum : values()) {
            PageColumnType pageColumnType = new PageColumnType();
            pageColumnType.setName(operateEnum.getName());
            pageColumnType.setDesc(operateEnum.getDesc());
            pageColumnType.setDataType(operateEnum.getDataType());
            list.add(pageColumnType);
        }
        return list;
    }

    /**
     * 对象
     */
    @Data
    public static class PageColumnType {
        public  String name;
        public  String desc;
        public  String dataType;
    }

}

package com.ruoyi.generator.sql;

/**
 * @ClassName SqlConstants
 * @Author Jumy
 * @Description //TODO
 * @Date 2020/10/5 13:50
 * @Version 1.0
 **/
public class SqlConstants {
    public static String SQL_TEMPLATE=
        "--------------------------------------------------------\n"
        +"--  DDL for Table <<TABLE_NAME>>\n"
        +"--------------------------------------------------------\n"
        +"\n"
        +"  CREATE TABLE <<TABLE_NAME>> \n"
        +"   (\n"
        +"<<COLUMN_DEFINE_LIST>>"
        +"   );\n"
        +"\n"
        +"<<COLUMN_COMMENT_LIST>>"
        +"   COMMENT ON TABLE <<TABLE_NAME>>  IS '<<TABLE_COMMENT>>';\n";
    //列定义模板
    public static String COLUMN_DEFINE_LIST_TEMPLATE="\t<<COLUMN_NAME>> <<COLUMN_TYPE>>, \n";
    //列注释模板
    public static String COLUMN_COMMENT_LIST_TEMPLATE="   COMMENT ON COLUMN <<TABLE_NAME>>.<<COLUMN_NAME>> IS '<<COLUMN_COMMENT>>';\n";
    //主键模板
    public static String PK_TEMPLATE=
            "\n--------------------------------------------------------\n"
            +"--  Constraints for Table <<TABLE_NAME>>\n"
            +"--------------------------------------------------------\n"
            +"   ALTER TABLE <<TABLE_NAME>> ADD CONSTRAINT <<TABLE_NAME>>_PK PRIMARY KEY (<<PRIMARY_KEY>>);\n";
    //序列模板
    public static String SEQ_TEMPLATE=
            "\n--------------------------------------------------------\n"
            +"--  DDL for Sequence SEQ_<<TABLE_NAME>>\n"
            +"--------------------------------------------------------\n"
            +"\n"
            + "   CREATE SEQUENCE  \"SEQ_<<TABLE_NAME>>\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 101 CACHE 20 NOORDER  NOCYCLE;\n";
    //是否是主键
    public static String IS_PK_SCOPE="Y,y,是,1";
}

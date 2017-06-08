package com.kairui.spider.core.utils.excel;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class ExcelHandler<T> implements XSSFSheetXMLHandler.SheetContentsHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Class<T> clazz;
    private Map<Integer, Field> fieldsMap = new HashMap<>();//定义一个map用于存放列的序号和field.

    private T row = null;
    private int index = -1;

    protected ExcelHandler(Class<T> clazz) throws Exception {
        this.clazz = clazz;
        List<Field> allFields = getMappedFiled(clazz, null);
        for (int col = 0; col < allFields.size(); col++) {
            Field field = allFields.get(col);
            //将有注解的field存放到map中.
            if (field.isAnnotationPresent(ExcelVOAttribute.class)) {
                field.setAccessible(true);//设置类的私有字段属性可访问.
                fieldsMap.put(col, field);
            }
        }
    }

    @Override
    public void startRow(int rowNum) {
        if (rowNum == index + 1) {
            index = rowNum;
        } else {
            log.error("第" + rowNum + "行数据为空");
            index = rowNum;
        }
        try {
            row = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endRow(int rowNum) {
        eachRow(rowNum, row);
    }

    public void eachRow(int rowNum, T row) {
    }

    @Override
    public void cell(String cell, String value, XSSFComment comment) {
        if (index == 0) return;
        int col = (new CellReference(cell)).getCol();
        Field field = fieldsMap.get(col);//从map中得到对应列的field.
        if (field == null) {
            log.info("匹配不到field {}", cell);
            return;
        }
        try {
            //取得类型,并根据对象类型设置值.
            Class<?> fieldType = field.getType();
            if (String.class == fieldType) {
                field.set(this.row, value);
            } else if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
                String temp = value;
                if (value.contains(".")) {
                    temp = value.substring(0, value.indexOf("."));
                }
                field.set(row, Integer.parseInt(temp));
            } else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
                field.set(row, Long.valueOf(value));
            } else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
                field.set(row, Float.valueOf(value));
            } else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
                field.set(row, Short.valueOf(value));
            } else if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {
                field.set(row, Double.valueOf(value));
            } else if (Date.class == fieldType) {
                field.set(row, value);
            } else if (Character.TYPE == fieldType) {
                if (value.length() > 0) {
                    field.set(row, value.charAt(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {

    }

    /**
     * 得到实体类所有通过注解映射了数据表的字段
     *
     * @param clazz  实体类
     * @param fields 所有字段
     * @return Map类型所有字段
     */
    private List<Field> getMappedFiled(Class clazz, List<Field> fields) {
        if (fields == null) {
            fields = new ArrayList<>();
        }

        Field[] allFields = clazz.getDeclaredFields();//得到所有定义字段
        //得到所有field并存放到一个list中.
        for (Field field : allFields) {
            if (field.isAnnotationPresent(ExcelVOAttribute.class)) {
                fields.add(field);
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            getMappedFiled(clazz.getSuperclass(), fields);
        }

        return fields;
    }
}
package com.ncu.chatserver.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FilePortUtil {

    /**
     * @Description 导出表格工具类
     * 基本思路：创建表格对象--->将数据set进表格--->将表格流写入response返回
     * @author 吴昊天
     * @date 2020/5/15 11:06
     */


    /**
     * 方法描述：导出功能
     * 注意：泛型T类字段名和containBean集合里字段名字的一致性
     * @author Harry
     * @date 2020/5/15  11:13
     * @param response
     * @param title       表名
     * @param headers     表头
     * @param list        数据集
     * @param containBean 数据集类型字段
     * @param <T>
     * @throws Exception
     * @return
     **/
    public static <T> void exportExcel(HttpServletResponse response, String title, String[] headers, List<T> list, List<String> containBean) throws Exception{
        HSSFWorkbook workbook = null;
        try {
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(title);
            HSSFRow row = sheet.createRow(0);
            //创建第一行表头
            for (short i = 0; i < headers.length; i++){
                HSSFCell cell = row.createCell(i);
                HSSFRichTextString textString = new HSSFRichTextString(headers[i]);
                cell.setCellValue(textString);
            }
            Iterator<T> it = list.iterator();
            int index = 0;
            while (it.hasNext()){
                index++;
                row = sheet.createRow(index);
                T t = (T)it.next();
                //通过反射得到字段
                Field[] fields = t.getClass().getDeclaredFields();
                //如果需要匹配
//                    if (CollectionUtils.isEmpty(containBean)){
                for (int j = 0; j < containBean.size(); j++){
                    for (int i = 0; i < fields.length; i++){
                        Field field  = fields[i];
                        if (!field.getName().equals(containBean.get(j)))
                            continue;
                        setCellValue(t,field,row,j);
                    }
                }
//                    } else {
//                        for (int i = 0; i < fields.length; i++){
//                            Field field = fields[i];
//                            setCellValue(t,field,row,i);
//                        }
//                    }
            }
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String((title).getBytes(), "ISO8859-1") + ".xls");
            workbook.write(response.getOutputStream());
        }finally {
            if (workbook != null){
                workbook.close();
            }
        }
    }

    /**
     * 方法描述：设置每一行中的列
     * @author Harry
     * @date 2020/5/15  11:44
     * @param t
     * @param field
     * @param row
     * @param index
     * @return T
     **/
    private static <T> void setCellValue(T t, Field field, HSSFRow row, int index) {
        HSSFCell cell = row.createCell(index);
        Object value = invoke(t, field);
        String textValue = null;
        if (value != null){
            if (value instanceof Date){
                Date date = (Date) value;
                textValue = DateFormatUtils.format(date,"yyyy-MM-dd HH:mm:ss");
            } else {
                textValue = value.toString();
            }
        }
        if (textValue != null){
            cell.setCellValue(textValue);
        }
    }

    /**
     * 方法描述：通过反射获取数据集字段
     * @author Harry
     * @date 2020/5/15  11:42
     * @param t 泛型
     * @param field
     * @return T
     **/
    private static <T> Object invoke(T t, Field field){
        try{
            String fieldName = field.getName();
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fieldName,t.getClass());
            Method readMethod = propertyDescriptor.getReadMethod();
            return readMethod.invoke(t);
        }catch (Exception e){
            return null;
        }
    }

}

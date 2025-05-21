package cn.com.otc.common.utils;

import cn.com.otc.common.annotation.export.EnableExport;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:导入导出工具类
 * @author: zhangliyan
 * @time: 2022/5/26
 */
@Slf4j
public class ExcelUtils {

    private static final ExcelUtils INSTANCE = new ExcelUtils();
    private  ExcelUtils(){}

    public static ExcelUtils geInstance(){
        return INSTANCE;
    }

    /**
     * 导出报表
     */
    public <T> void exportExcel(HttpServletResponse response,List<T> dataList,Class<?> clz){

        EnableExport enableExport =  clz.getAnnotation(EnableExport.class);
        if(enableExport == null){
            throw new RRException(ResultCodeEnum.EXPORT_ERROR_2001.msg,
                    ResultCodeEnum.EXPORT_ERROR_2001.code);
        }
        String fileName =enableExport.fileName()+"_"+System.currentTimeMillis();

        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try {
            EasyExcel.write(response.getOutputStream(), clz).sheet(enableExport.sheetName()).doWrite(dataList);
        }catch (Exception ex){
            log.error(">>> 导出数据异常：{}", ex.getMessage());
        }
    }


    /**
     * 导出报表
     */
    public <T> void exportExcel(List<T> dataList,Class<?> clz){
        EnableExport enableExport =  clz.getAnnotation(EnableExport.class);
        if(enableExport == null){
            throw new RRException(ResultCodeEnum.EXPORT_ERROR_2001.msg,
                    ResultCodeEnum.EXPORT_ERROR_2001.code);
        }
        String fileName ="D:"+File.separator+"export"+File.separator+
                enableExport.fileName()+File.separator+enableExport.fileName()+"_"+
                System.currentTimeMillis() + ".xlsx";
        /**
         * 判断文件夹是否存在，不存在则创建
         */
        createFile(fileName);
        /**
         * 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
         */
        EasyExcel.write(fileName, clz).sheet(0,enableExport.sheetName()).doWrite(dataList);
    }


    /**
     * 导出报表
     */
    public <T> void exportExcelSheet(List<T> dataList,Class<?> clz){
        EnableExport enableExport =  clz.getAnnotation(EnableExport.class);
        if(enableExport == null){
            throw new RRException(ResultCodeEnum.EXPORT_ERROR_2001.msg,
                    ResultCodeEnum.EXPORT_ERROR_2001.code);
        }
        String fileName ="D:"+File.separator+"export"+File.separator+
                enableExport.fileName()+File.separator+enableExport.fileName()+"_"+
                System.currentTimeMillis() + ".xlsx";
        /**
         * 判断文件夹是否存在，不存在则创建
         */
        createFile(fileName);
        WriteTable writeTable = EasyExcel.writerTable(1).needHead(Boolean.TRUE).head(clz).build();
        ExcelWriter excelWriter = EasyExcel.write(fileName, clz).build();
        WriteSheet writeSheet = new WriteSheet();
        writeSheet.setSheetNo(1);
        writeSheet.setSheetName("aa");
        excelWriter.write(dataList,writeSheet,writeTable);
        writeSheet = new WriteSheet();
        writeSheet.setSheetNo(2);
        writeSheet.setSheetName("bb");
        excelWriter.write(dataList,writeSheet,writeTable);
        excelWriter.finish();

        String fileNameNew ="D:"+File.separator+"export"+File.separator+
                enableExport.fileName()+File.separator+enableExport.fileName()+"_"+
                System.currentTimeMillis() + ".csv";

        excelToCsv(fileName,fileNameNew);
    }



    public <T> void importExport(String fileName,Class<?> clz){

        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取3000条数据 然后返回过来 直接调用使用数据就行
        EasyExcel.read(fileName, clz, new PageReadListener<T>(dataList -> {
            for (T entity : dataList) {
                System.out.println("读取到一条数据-->"+JSONUtil.toJsonStr(entity));
            }
        })).sheet().doRead();
    }

    private  void createFile(String fileName) {
        File file = new File(fileName);
        //判断文件或文件夹是否存在
        if(!file.exists())
        {
            File fileParent = file.getParentFile();
            if(!fileParent.exists()){
                fileParent.mkdirs();
            }
            //文件夹不存在就要新建文件
            try {
                file.createNewFile();
            }catch (Exception e){
              throw new RRException(ResultCodeEnum.EXPORT_ERROR_2002.msg,
                        ResultCodeEnum.EXPORT_ERROR_2002.code,e);
            }
        }
    }


    /**
     * 将excel表格转成csv格式
     *
     * @param oldFilePath
     * @param newFilePath
     */
    public static void excelToCsv(String oldFilePath, String newFilePath) {
        String buffer = "";
        Workbook wb = null;
        Sheet sheet = null;
        Row row = null;
        Row rowHead = null;
        List<Map<String, String>> list = null;
        String cellData = null;
        String filePath = oldFilePath;

        wb = readExcel(filePath);
        if (wb != null) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                sheet = wb.getSheetAt(i);
                // 标题总列数
                rowHead = sheet.getRow(i);
                if (rowHead == null) {
                    continue;
                }
                //总列数colNum
                int colNum = rowHead.getPhysicalNumberOfCells();
                String[] keyArray = new String[colNum];
                Map<String, Object> map = new LinkedHashMap<>();

                //用来存放表中数据
                list = new ArrayList<Map<String, String>>();
                //获取第一个sheet
                sheet = wb.getSheetAt(i);
                //获取最大行数
                int rownum = sheet.getPhysicalNumberOfRows();
                //获取第一行
                row = sheet.getRow(0);
                //获取最大列数
                int colnum = row.getPhysicalNumberOfCells();
                for (int n = 0; n < rownum; n++) {
                    row = sheet.getRow(n);
                    for (int m = 0; m < colnum; m++) {
                        //cellData = getCellFormatValue(row.getCell(m)).toString();
                        buffer += cellData;
                    }
                    buffer = buffer.substring(0, buffer.lastIndexOf(","));
                    buffer += "\n";
                }

                String savePath = newFilePath;
                File saveCSV = new File(savePath);
                try {
                    if (!saveCSV.exists()) {
                        saveCSV.createNewFile();
                    }
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveCSV));
                    writer.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF }));
                    writer.write(buffer);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

    }


    //读取excel
    public static Workbook readExcel(String filePath) {
        Workbook wb = null;
        if (filePath == null) {
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                return wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                return wb = new XSSFWorkbook(is);
            } else {
                return wb = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }

    /*public static Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            //判断cell类型
            int cellType = cell.getCellType().ordinal();
            switch (cell.getCellType().ordinal()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    String cellva = getValue(cell);
                    cellValue = cellva.replaceAll("\n", " ") + ",";
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    //判断cell是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        //转换为日期格式YYYY-mm-dd
                        cellValue = String.valueOf(cell.getDateCellValue()).replaceAll("\n", " ") + ",";
                    } else {
                        //数字
                        cellValue = String.valueOf(cell.getNumericCellValue()).replaceAll("\n", " ") + ",";
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = String.valueOf(cell.getRichStringCellValue()).replaceAll("\n", " ") + ",";
                    break;
                }
                default:
                    cellValue = "" + ",";
            }
        } else {
            cellValue = "" + ",";
        }
        return cellValue;
    }*/

    /**
     * 此方法为去掉转csv时数字等默认加上的小数点
     * 如果不需要刻意不调用此方法
     */
    /*public static String getValue(Cell hssfCell) {
        if (hssfCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            // 返回布尔类型的值
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            // 返回数值类型的值
            Object inputValue = null;// 单元格值
            Long longVal = Math.round(hssfCell.getNumericCellValue());
            Double doubleVal = hssfCell.getNumericCellValue();
            if (Double.parseDouble(longVal + ".0") == doubleVal) {   //判断是否含有小数位.0
                inputValue = longVal;
            } else {
                inputValue = doubleVal;
            }
            DecimalFormat df = new DecimalFormat("#");    //在此处更改小数点及位数，按自己需求选择；
            return String.valueOf(df.format(inputValue));      //返回String类型
        } else {
            // 返回字符串类型的值
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }*/

}

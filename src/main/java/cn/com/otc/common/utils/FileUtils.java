package cn.com.otc.common.utils;

import java.io.File;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/6/8
 */
public class FileUtils {

    public static Boolean deleteFile(File file) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            return true;
        }
        //获取目录下子文件
        File[] files = file.listFiles();
        //遍历该目录下的文件对象
        for (File f : files) {
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory()) {
                //递归删除目录下的文件
                deleteFile(f);
            } else {
                //文件删除
                f.delete();
            }
        }
        //文件夹删除
        file.delete();
        return true;
    }
}

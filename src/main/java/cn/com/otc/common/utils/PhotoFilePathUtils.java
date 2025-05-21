package cn.com.otc.common.utils;

import cn.com.otc.common.config.HttpClientManager;
import cn.com.otc.common.config.MyCommonConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.UUID;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/6/8
 */
@Slf4j
@Component
public class PhotoFilePathUtils {


    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;


    // 阿里云 OSS 配置
    private static final String OSS_BUCKET_NAME = "packet-gift";

    /*
    * 下载文件
    * */
    public byte[] downloadAvatar(String avatarUrl) throws IOException {
        //try  {
            HttpGet request = new HttpGet(avatarUrl);
            try (CloseableHttpResponse response = HttpClientManager.getHttpClient().execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (InputStream inputStream = entity.getContent();
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                        return outputStream.toByteArray(); // 返回头像的字节数组
                    }
                }
            }
        //}
        throw new IOException("无法下载头像");
    }


    /**
     * 上传数据到 OSS
     *
     * @param data  要上传的数据
     * @param image OSS 对象名称
     */
    public  String uploadToOss(byte[] data, String image) {
        int dotIndex = image.lastIndexOf(".");
        String fileType = "";
        if (dotIndex > 0 && dotIndex < image.length() - 1) {
            // 确保`.`不是文件路径的第一个字符且不是最后一个字符
            fileType = image.substring(dotIndex);
            System.out.println("File type: " + fileType);
        } else {
            // 如果没有找到`.`或者`.`在文件路径的开头或结尾，则表示没有有效的扩展名
            fileType = "";
            System.out.println("No valid file type found.");
        }
        String newName = UUID.randomUUID() + fileType;


        // 创建 OSSClient 实例
        String uploadUrl = null;
        OSS ossClient = new OSSClientBuilder().build(myCommonConfig.getOssEndpoint(), myCommonConfig.getOssAccessKeyId(), myCommonConfig.getOssAccessKeySecret());
        try {
            // 上传内容到指定的存储空间（bucketName）并保存为指定的文件名称（objectName）
            ossClient.putObject(OSS_BUCKET_NAME, newName, new ByteArrayInputStream(data));
            ossClient.setObjectAcl(OSS_BUCKET_NAME, newName, CannedAccessControlList.PublicRead);
            uploadUrl = "https://" + OSS_BUCKET_NAME + "." + myCommonConfig.getOssEndpoint() + "/" + newName;
            return uploadUrl;
        } catch (Exception e) {
            log.error("上传文件异常：{}", e.getMessage(), e);
            return "";
        } finally {
            // 关闭 OSSClient
            ossClient.shutdown();
        }
    }
}

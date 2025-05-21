package cn.com.otc.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @description:校验token
 * @author: zhangliyan
 * @time: 2024/2/21
 */
@Slf4j
@Component
public class ImageUtil {

  public String imageToBase64(String imageUrl) throws IOException {
    byte[] imageData = downloadImage(imageUrl);
    String base64Image = convertToBase64(imageData);
    String base64ImageDataUri = "data:jpg;base64," + base64Image;
    return base64ImageDataUri;
  }

  // 从远程 URL 下载图片并转换为字节数组
  public byte[] downloadImage(String imageUrl) throws IOException {
    URL url = new URL(imageUrl);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (InputStream in = url.openStream()) {
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }
    }
    return baos.toByteArray();
  }

  // 将字节数组转换为 Base64 编码的字符串
  public String convertToBase64(byte[] imageData) {
    return Base64.getEncoder().encodeToString(imageData);
  }
}

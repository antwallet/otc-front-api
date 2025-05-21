package cn.com.otc.common.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @Auther: 2024
 * @Date: 2024/8/6 14:28
 * @Description: 图片生成unicode
 */
public class ImageToUnicode {
    public static void main(String[] args) {
        try {
            String imagePath = "D:\\有用的东西！！！！\\640X290.gif"; // 图片路径
            String output = imageToUnicode(ImageIO.read(new File(imagePath)), 1);
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String imageToUnicode(BufferedImage image, int scale) {
        StringBuilder sb = new StringBuilder();
        int width = image.getWidth() / scale;
        int height = image.getHeight() / scale;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 获取缩放后的像素值
                int rgb = image.getRGB(x * scale, y * scale);
                // 将RGB值转换为灰度值
                int brightness = (int) (0.2126 * ((rgb >> 16) & 0xff) +
                        0.7152 * ((rgb >> 8) & 0xff) +
                        0.0722 * (rgb & 0xff));
                // 将灰度值映射到Unicode字符
                char c = (char) (' ' + (brightness > 128 ? 127 - brightness / 2 : brightness));
                sb.append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

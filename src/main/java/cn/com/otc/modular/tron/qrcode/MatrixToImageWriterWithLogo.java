package cn.com.otc.modular.tron.qrcode;


import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang3.StringUtils;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @description:模仿Google的MatrixToImageWriter自己编写的类
 * @author: zhangliyan
 * @time: 2024/2/28
 */
public class MatrixToImageWriterWithLogo {

  private static final MatrixToImageConfig config = new MatrixToImageConfig();

  private MatrixToImageWriterWithLogo() {
  }

  public static BufferedImage toBufferedImage(BitMatrix matrix,MatrixToImageConfig config, String declareText) {
    int width = matrix.getWidth();
    int height = matrix.getHeight();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        image.setRGB(x, y,  (matrix.get(x, y) ? config.getPixelOnColor() : config.getPixelOffColor()));
      }
    }

    if(StringUtils.isNotBlank(declareText)){
      addFontImage(image,declareText);
    }
    return image;
  }

  private static void addFontImage(BufferedImage source, String declareText) {
    //生成image
    int defineWidth = 300;
    int defineHeight = 20;
    BufferedImage textImage = new BufferedImage(defineWidth, defineHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) textImage.getGraphics();
    //开启文字抗锯齿
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setBackground(Color.WHITE);
    g2.clearRect(0, 0, defineWidth, defineHeight);
    g2.setPaint(Color.BLACK);
    FontRenderContext context = g2.getFontRenderContext();
    //部署linux需要注意 linux无此字体会显示方块
    Font font = new Font("宋体", Font.BOLD, 12);
    g2.setFont(font);
    LineMetrics lineMetrics = font.getLineMetrics(declareText, context);
    FontMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
    float offset = (defineWidth - fontMetrics.stringWidth(declareText)) / 2;
    float y = (defineHeight + lineMetrics.getAscent() - lineMetrics.getDescent() - lineMetrics.getLeading()) / 2;
    g2.drawString(declareText, (int) offset, (int) y);

    Graphics2D graph = source.createGraphics();
    //开启文字抗锯齿
    graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    //添加image
    int width = textImage.getWidth(null);
    int height = textImage.getHeight(null);

    Image src = textImage;
    graph.drawImage(src, 0, 300 - 20, width, height, Color.WHITE, null);
    graph.dispose();
  }



  public static Color getGradientColor(int x, int y, int width, int height) {
    float hue = (float) x / width;
    float saturation = 1.0f;
    float brightness = 1.0f - (float) y / height;
    return Color.getHSBColor(hue, saturation, brightness);
  }

  public static void writeToPath(BitMatrix matrix, String format, Path path, Path logoPath) throws IOException {
    OutputStream os = new FileOutputStream(path.toFile());
    writeToStream(matrix,format,os,logoPath,config);
  }

  public static void writeToPath(BitMatrix matrix, String format, Path path, Path logoPath,String declareText) throws IOException {
    OutputStream os = new FileOutputStream(path.toFile());
    writeToStream(matrix,format,os,logoPath,config,declareText);
  }


  public static void writeToPath(BitMatrix matrix, String format, Path path, Path logoPath, MatrixToImageConfig config) throws IOException {
    OutputStream os = new FileOutputStream(path.toFile());
    writeToStream(matrix,format,os,logoPath,config);
  }

  public static void writeToPath(BitMatrix matrix, String format, Path path, Path logoPath, MatrixToImageConfig config,String declareText) throws IOException {
    OutputStream os = new FileOutputStream(path.toFile());
    writeToStream(matrix,format,os,logoPath,config,declareText);
  }

  public static void writeToPath(BitMatrix matrix, String format, Path path, URL logoUrl) throws IOException {
    OutputStream os = new FileOutputStream(path.toFile());
    writeToStream(matrix,format,os,logoUrl,config);
  }

  public static void writeToPath(BitMatrix matrix, String format, Path path, URL logoUrl,MatrixToImageConfig config) throws IOException {
    OutputStream os = new FileOutputStream(path.toFile());
    writeToStream(matrix,format,os,logoUrl,config);
  }


  public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, Path logoPath) throws IOException {
    writeToStream(matrix, format, stream, logoPath,config);
  }


  public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, Path logoPath,MatrixToImageConfig config) throws IOException {
    BufferedImage image = toBufferedImage(matrix,config,"");
    //设置logo图标
    BufferedImage bi = ImageIO.read(logoPath.toFile());
    image = logoMatrix(image,bi);

    if (!ImageIO.write(image, format, stream)) {
      throw new IOException("Could not write an image of format " + format);
    }
  }

  public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, Path logoPath,MatrixToImageConfig config,String declareText) throws IOException {
    BufferedImage image = toBufferedImage(matrix,config,declareText);
    //设置logo图标
    BufferedImage bi = ImageIO.read(logoPath.toFile());
    image = logoMatrix(image,bi);

    if (!ImageIO.write(image, format, stream)) {
      throw new IOException("Could not write an image of format " + format);
    }
  }

  public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, URL logoUrl) throws IOException {
    writeToStream(matrix,format,stream,logoUrl,config);
  }


  public static void writeToStream(BitMatrix matrix, String format, OutputStream stream, URL logoUrl,MatrixToImageConfig config) throws IOException {
    BufferedImage image = toBufferedImage(matrix,config,"");
    //设置logo图标
    BufferedImage bi = ImageIO.read(logoUrl);
    image = logoMatrix(image,bi);

    if (!ImageIO.write(image, format, stream)) {
      throw new IOException("Could not write an image of format " + format);
    }
  }


  /**
   * 设置 logo
   * @param matrixImage 源二维码图片
   * @return 返回带有logo的二维码图片
   * @author Administrator sangwenhao
   * @author zgd
   * @date 2019/8/16 14:44
   */
  private static BufferedImage logoMatrix(BufferedImage matrixImage, BufferedImage logo) {
    //读取二维码图片，并构建绘图对象
    Graphics2D g2 = matrixImage.createGraphics();

    int matrixWidth = matrixImage.getWidth();
    int matrixHeigh = matrixImage.getHeight();

    //绘制
    g2.drawImage(logo, matrixWidth / 5 * 2, matrixHeigh / 5 * 2, matrixWidth / 5, matrixHeigh / 5, null);
    BasicStroke stroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    // 设置笔画对象
    g2.setStroke(stroke);
    //指定弧度的圆角矩形
    RoundRectangle2D.Float round = new RoundRectangle2D.Float(matrixWidth / 5 * 2, matrixHeigh / 5 * 2, matrixWidth / 5, matrixHeigh / 5, 20, 20);
    g2.setColor(Color.white);
    // 绘制圆弧矩形
    g2.draw(round);

    //设置logo 有一道灰色边框
    BasicStroke stroke2 = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    // 设置笔画对象
    g2.setStroke(stroke2);
    RoundRectangle2D.Float round2 = new RoundRectangle2D.Float(matrixWidth / 5 * 2 + 2, matrixHeigh / 5 * 2 + 2, matrixWidth / 5 - 4, matrixHeigh / 5 - 4, 20, 20);
    g2.setColor(new Color(128, 128, 128));
    // 绘制圆弧矩形
    g2.draw(round2);
    g2.dispose();
    matrixImage.flush();
    return matrixImage;
  }
}

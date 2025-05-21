package cn.com.otc.modular.tron;

import cn.com.otc.modular.tron.qrcode.BitMatrixBuilder;
import cn.com.otc.modular.tron.qrcode.QRBarCodeUtil;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/2/28
 */
public class TestDemo {

  public static void main(String[] args) throws IOException {
      Path path2 = Paths.get("C:\\Users\\zhangliyan\\Desktop\\logo1.png");
//    Path path = Paths.get("C:\\Users\\zhangliyan\\Desktop\\tg_qrcode.png");
//    BitMatrix matrix = BitMatrixBuilder.create().setContent("TYfzi7tPPXwEDUDC2iEC7DyFbZYuUn1Z7i").build();
//    QRBarCodeUtil.createLogoAndTextQrCodeToPath(matrix,path,path2,"TYfzi7tPPXwEDUDC2iEC7DyFbZYuUn1Z7i");
    ByteArrayOutputStream bof = new ByteArrayOutputStream();
    BitMatrix matrix = BitMatrixBuilder.create().setContent("TYfzi7tPPXwEDUDC2iEC7DyFbZYuUn1Z7i").build();
    QRBarCodeUtil.createLogoQrCodeToStream(matrix,bof,path2);
    String base64 = QRBarCodeUtil.imageToBase64(bof.toByteArray());
    System.out.println("生成的二维码:"+base64);
  }
}

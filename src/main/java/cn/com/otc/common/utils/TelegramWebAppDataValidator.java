package cn.com.otc.common.utils;

/**
 * @Auther: 2024
 * @Date: 2024/9/11 17:50
 * @Description:
 */

import cn.com.otc.common.config.MyCommonConfig;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TelegramWebAppDataValidator {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String WEB_APP_DATA = "WebAppData";
    private static final Logger log = LoggerFactory.getLogger(TelegramWebAppDataValidator.class);

    @Autowired
    private MyCommonConfig myCommonConfig;

    private static final String WEB_APP_DATA_KEY = "WebAppData";

    public  boolean validateMiniAppData(String dataString, String userTGID) {
        Map<String, String> data;
        try {
            data = parseDataString(dataString);
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            return false;
        }

        // 判断传入的tgId是否与用户信息中的tgId一致
        JSONObject userJson = new JSONObject(URLDecoder.decode(data.get("user"), StandardCharsets.UTF_8));
        long id = userJson.getLong("id");

        // 判断 id 是否相同
        if (!String.valueOf(id).equals(userTGID)) {
            System.out.println("IDs are the same.");
            return false;
        }
        String hash = data.remove("hash");

        // Build the data-check-string
        String dataCheckString = data.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue().replace("\n", "\\n"))
                .collect(Collectors.joining("\n"));

        // Calculate the secret key
        byte[] secretKey = hmacSha256(myCommonConfig.getTgHttpTgToken().getBytes(StandardCharsets.UTF_8), WEB_APP_DATA_KEY.getBytes(StandardCharsets.UTF_8));

        // Validate the hash
        byte[] expectedHash = hmacSha256(dataCheckString.getBytes(StandardCharsets.UTF_8), secretKey);
        return Arrays.equals(hexToBytes(hash), expectedHash);
    }

    private  Map<String, String> parseDataString(String dataString) throws URISyntaxException, UnsupportedEncodingException {
        Map<String, String> data = new URIBuilder("http://example.com?" + dataString)
                .getQueryParams()
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getName(),
                        p -> StringUtils.remove(p.getValue(), "%22")
                ));
        return data;
    }

    private  byte[] hmacSha256(byte[] data, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private  byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}


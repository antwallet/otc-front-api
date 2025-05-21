package cn.com.otc.test;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException {

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };


    }
}
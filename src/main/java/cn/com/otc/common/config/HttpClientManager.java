package cn.com.otc.common.config;

import lombok.Getter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClientManager {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientManager.class);

    @Getter
    private static volatile CloseableHttpClient httpClient;

    private static volatile boolean isClosed = false;

    private static final PoolingHttpClientConnectionManager connectionManager;
    private static final IdleConnectionEvictor connectionEvictor;

    static {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);
        connectionManager.setValidateAfterInactivity(30000); // 30 seconds

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setConnectionRequestTimeout(5000)
                        .build())
                .build();

        connectionEvictor = new IdleConnectionEvictor(connectionManager);
        connectionEvictor.start();

        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(HttpClientManager::closeHttpClient));
    }

    // 提供关闭方法，避免重复关闭
    public static synchronized void closeHttpClient() {
        if (!isClosed) {
            try {
                connectionEvictor.shutdown();
                connectionEvictor.awaitTermination(5, TimeUnit.SECONDS);
                if (httpClient != null) {
                    httpClient.close();
                }
                connectionManager.close();
                isClosed = true;
                logger.info("HttpClient has been successfully closed.");
            } catch (IOException | InterruptedException e) {
                logger.error("Error occurred while closing HttpClient", e);
            }
        }
    }

    public static void logPoolStats() {
        logger.info("Total stats of connection pool: " +
                "Available = " + connectionManager.getTotalStats().getAvailable() + ", " +
                "Leased = " + connectionManager.getTotalStats().getLeased() + ", " +
                "Pending = " + connectionManager.getTotalStats().getPending() + ", " +
                "Max = " + connectionManager.getTotalStats().getMax());
    }

    private static class IdleConnectionEvictor extends Thread {
        private final PoolingHttpClientConnectionManager connectionManager;
        private volatile boolean shutdown;

        public IdleConnectionEvictor(PoolingHttpClientConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                logger.warn("IdleConnectionEvictor thread interrupted", ex);
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }

        public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            join(unit.toMillis(timeout));
        }
    }
}

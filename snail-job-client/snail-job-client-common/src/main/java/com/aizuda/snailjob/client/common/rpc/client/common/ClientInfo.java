package com.aizuda.snailjob.client.common.rpc.client.common;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.log.SnailJobLog;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: ylchai
 */
public final class ClientInfo {
    public static final String HOST_ID = IdUtil.getSnowflake().nextIdStr();

    /**
     * 服务端端口
     */
    private static final String SNAIL_JOB_SERVER_PORT = "snail-job.server.port";
    /**
     * 服务端host
     */
    private static final String SNAIL_JOB_SERVER_HOST = "snail-job.server.host";

    /**
     * 客户端端口
     */
    private static final String SNAIL_JOB_CLIENT_PORT = "snail-job.port";

    private static final Integer MIN_PORT = 15000;
    private static final Integer MAX_PORT = 50000;
    private static final ReentrantLock PORT_LOCK = new ReentrantLock();
    private static final Integer RANDOM_CLIENT_PORT = -1;

    private static final int PORT;

    static {
        PORT = Integer.parseInt(System.getProperty(SNAIL_JOB_CLIENT_PORT, String.valueOf(1789)));
    }

    public static String getClientHost() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
        return snailJobProperties.getHost();
    }

    /**
     * 获取服务端端口
     *
     * @return port
     */
    public static int getServerPort() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getContext().getBean(SnailJobProperties.class);
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();

        String port = System.getProperty(SNAIL_JOB_SERVER_PORT);
        if (StrUtil.isBlank(port)) {
            System.setProperty(SNAIL_JOB_SERVER_PORT, String.valueOf(serverConfig.getPort()));
        }

        return Integer.parseInt(System.getProperty(SNAIL_JOB_SERVER_PORT));
    }

    /**
     * 获取服务端host
     *
     * @return host
     */
    public static String getServerHost() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
        SnailJobProperties.ServerConfig serverConfig = snailJobProperties.getServer();

        String host = System.getProperty(SNAIL_JOB_SERVER_HOST);
        if (StrUtil.isBlank(host)) {
            System.setProperty(SNAIL_JOB_SERVER_HOST, serverConfig.getHost());
        }

        return System.getProperty(SNAIL_JOB_SERVER_HOST);
    }

    /**
     * 获取客户端端口
     *
     * @return port 端口
     */
    public static Integer getClientPort() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);

        Integer port = snailJobProperties.getPort();
        // 如果端口设置为随机端口，则获取可用端口
        if (port.equals(RANDOM_CLIENT_PORT)) {
            // 使用随机算法获取端口
            PORT_LOCK.lock();
            try {
                // 双重检查，避免重复获取端口
                if (snailJobProperties.getPort().equals(RANDOM_CLIENT_PORT)) {
                    port = getAvailablePort();
                    snailJobProperties.setPort(port);
                    SnailJobLog.LOCAL.info("snail job client port :{}", port);
                } else {
                    port = snailJobProperties.getPort();
                }
            } finally {
                PORT_LOCK.unlock();
            }
        }

        return port;
    }


    /**
     * 获取随机可用的端口
     *
     * @return 可用端口号
     */
    private static Integer getAvailablePort() {
        int port;
        do {
            port = MIN_PORT + (int) (Math.random()*(MAX_PORT - MIN_PORT));
        }while (!isPortAvailable(port));

        return port;
    }

    /**
     * 检查端口是否可以使用
     *
     * @param port 端口号
     * @return 是否可用
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            // 设置端口重用
            serverSocket.setReuseAddress(true);
            // 绑定端口
            serverSocket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

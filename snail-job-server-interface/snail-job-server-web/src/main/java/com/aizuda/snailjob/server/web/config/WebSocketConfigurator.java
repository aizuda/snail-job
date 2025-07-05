package com.aizuda.snailjob.server.web.config;

import com.aizuda.snailjob.common.core.exception.SnailJobAuthenticationException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.template.datasource.persistence.po.SystemUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.List;
import java.util.Map;

/**
 * WebScoket配置处理器
 * @since 1.5.0
 */
@Configuration
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {
    public static final String AUTHENTICATION = "Snail-Job-Auth";
    public static final String SID = "sid";
    public static final String SCENE = "scene";
    public static final String USER_INFO = "user-info";

    /**
     * ServerEndpointExporter 作用
     *
     * 这个Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> parameterMap = request.getParameterMap();
        String queryString = request.getQueryString();

        // 获取 token 中的 user id
        SystemUser systemUser;
        try {
            String token = parameterMap.get(AUTHENTICATION).get(0);
            systemUser = JsonUtil.parseObject(JWT.decode(token).getAudience().get(0), SystemUser.class);
        } catch (JWTDecodeException j) {
            throw new SnailJobAuthenticationException("Login expired, please login again");
        }

        String sid = parameterMap.get(SID).get(0);
        String scene = parameterMap.get(SCENE).get(0);
        Map<String, Object> userProperties = sec.getUserProperties();

        userProperties.put(SID, sid);
        userProperties.put(SCENE, scene);
        userProperties.put(USER_INFO, systemUser);

    }

    /**
     * 初始化端点对象,也就是被@ServerEndpoint所标注的对象
     */
    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return super.getEndpointInstance(clazz);
    }


}
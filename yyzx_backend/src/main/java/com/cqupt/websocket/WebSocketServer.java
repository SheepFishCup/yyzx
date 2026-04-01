package com.cqupt.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{userId}")
@Slf4j
public class WebSocketServer {
    // 每次有新的客户端连接，WebSocket 容器都会创建一个新的 WebSocketServer 实例
    // 所以必须要用static修饰才能保证消息推送成功
    private final static Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    // 存储用户角色信息（用于按角色推送）
    private final static Map<String, Integer> USER_ROLES = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // 将 userId 和 session 绑定存储
        SESSIONS.put(userId, session);
        log.info("建立连接，sessionId=" + session.getId());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("收到来自客户端：" + userId + "的信息:" + message);
    }


    public void sendToAllClient(String message) {
        Collection<Session> session = SESSIONS.values();
        for (Session s : session) {
            try {
                //服务器向客户端发送消息
                log.info("推送消息" + message);
                s.getBasicRemote().sendText(message);
                log.info("消息推送成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 推送给指定用户
     */
    public void sendToUser(String userId, String message) {
        //判断用户是否在线
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("推送消息给用户 {} 成功", userId);
            } catch (Exception e) {
                log.error("推送消息给用户 {} 失败", userId, e);
            }
        } else {
            log.warn("用户 {} 不在线", userId);
        }
    }

    /**
     * 推送给指定角色的所有用户
     */
    public void sendToRole(Integer roleId, String message) {
        for (Map.Entry<String, Session> entry : SESSIONS.entrySet()) {
            String userId = entry.getKey();
            Integer userRole = USER_ROLES.get(userId);

            if (userRole != null && userRole.equals(roleId)) {
                try {
                    entry.getValue().getBasicRemote().sendText(message);
                    log.info("推送消息给角色 {} 的用户 {} 成功", roleId, userId);
                } catch (Exception e) {
                    log.error("推送消息给角色 {} 的用户 {} 失败", roleId, userId, e);
                }
            }
        }
    }

    /**
     * 记录用户角色（在登录时调用）
     */
    public void setUserRole(String userId, Integer roleId) {
        USER_ROLES.put(userId, roleId);
    }

    /**
     * 用户断开连接时清除角色信息
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId)  {
        log.info("连接断开:" + userId);
        SESSIONS.remove(userId);
        USER_ROLES.remove(userId);
    }
}

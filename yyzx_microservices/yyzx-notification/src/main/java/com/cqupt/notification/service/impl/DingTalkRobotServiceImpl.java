package com.cqupt.notification.service.impl;

import com.cqupt.notification.config.DingTalkConfig;
import com.cqupt.notification.service.DingTalkRobotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DingTalkRobotServiceImpl implements DingTalkRobotService {

    @Autowired
    private DingTalkConfig dingTalkConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendTextMessage(String content) {
        try {
            String webhook = getSignedWebhook();

            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "text");

            Map<String, String> text = new HashMap<>();
            text.put("content", content);
            message.put("text", text);

            String jsonBody = objectMapper.writeValueAsString(message);

            HttpPost post = new HttpPost(webhook);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = org.apache.http.util.EntityUtils.toString(
                        response.getEntity(), "UTF-8");

                if (statusCode == 200) {
                    com.fasterxml.jackson.databind.JsonNode jsonNode =
                            objectMapper.readTree(responseBody);
                    int errCode = jsonNode.has("errcode")
                            ? jsonNode.get("errcode").asInt() : -1;
                    if (errCode == 0) {
                        log.info("钉钉机器人消息发送成功");
                    } else {
                        String errMsg = jsonNode.has("errmsg")
                                ? jsonNode.get("errmsg").asText() : "未知错误";
                        log.error("钉钉 API 返回错误：errcode={}, errmsg={}", errCode, errMsg);
                    }
                } else {
                    log.error("钉钉机器人消息发送失败，状态码：{}", statusCode);
                }
            }
        } catch (Exception e) {
            log.error("发送钉钉机器人消息异常：{}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMarkdownMessage(String title, String text) {
        try {
            String webhook = getSignedWebhook();

            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "markdown");

            Map<String, String> markdown = new HashMap<>();
            markdown.put("title", title);
            markdown.put("text", text);
            message.put("markdown", markdown);

            String jsonBody = objectMapper.writeValueAsString(message);

            HttpPost post = new HttpPost(webhook);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    log.info("钉钉机器人 Markdown 消息发送成功");
                } else {
                    log.error("钉钉机器人 Markdown 消息发送失败，状态码：{}", statusCode);
                }
            }
        } catch (Exception e) {
            log.error("发送钉钉机器人 Markdown 消息异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 获取加签后的 Webhook URL
     */
    private String getSignedWebhook() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (dingTalkConfig.getSecret() != null && !dingTalkConfig.getSecret().isEmpty()) {
            String stringToSign = timestamp + "\n" + dingTalkConfig.getSecret();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    dingTalkConfig.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(
                    new String(Base64.getEncoder().encode(signData)), "UTF-8");

            return dingTalkConfig.getWebhook() + "&timestamp=" + timestamp + "&sign=" + sign;
        }
        return dingTalkConfig.getWebhook();
    }
}

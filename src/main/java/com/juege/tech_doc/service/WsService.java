package com.juege.tech_doc.service;

import com.juege.tech_doc.TechDocApplication;
import com.juege.tech_doc.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WsService {

    @Resource
    public WebSocketServer webSocketServer;

    private static final Logger LOG = LoggerFactory.getLogger(TechDocApplication.class);

    @Async
    public void sendInfo(String message, String logId) {
        //需要跟调用方在不同类中
        MDC.put("LOG_ID", logId);
        LOG.info(message+"！！!");
        webSocketServer.sendInfo(message);
    }
}

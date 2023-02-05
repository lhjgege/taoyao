package com.acgist.taoyao.signal.protocol.platform;

import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;

import com.acgist.taoyao.boot.model.Message;
import com.acgist.taoyao.signal.client.ClientSession;
import com.acgist.taoyao.signal.event.platform.ShutdownEvent;
import com.acgist.taoyao.signal.protocol.ProtocolMapAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * 关闭服务信令
 * 
 * @author acgist
 */
@Slf4j
public class ShutdownProtocol extends ProtocolMapAdapter {

	public static final Integer PID = 1000;
	
	public ShutdownProtocol() {
		super(PID, "关闭服务信令");
	}

	@Override
	public void execute(String sn, Map<?, ?> body, Message message, ClientSession session) {
		// 推送事件
		this.publishEvent(new ShutdownEvent(sn, body, message, session));
		// 全员广播
		this.clientSessionManager.broadcast(message);
		// 关闭信令服务
		if(this.context instanceof ConfigurableApplicationContext context) {
			log.info("关闭信令服务：{}", sn);
			if(context.isActive()) {
				// 如果需要完整广播可以设置延时
				context.close();
			}
		} else {
			log.info("关闭服务失败：{}", sn);
		}
	}

}

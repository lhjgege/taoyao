package com.acgist.taoyao.signal.event;

import java.util.Map;

import com.acgist.taoyao.boot.model.Message;
import com.acgist.taoyao.signal.client.Client;

import lombok.Getter;

/**
 * 终端事件适配器
 * 
 * @author acgist
 */
@Getter
public abstract class ClientEventAdapter extends ApplicationEventAdapter {
	
	private static final long serialVersionUID = 1L;

	/**
	 * 终端
	 */
	private final Client client;
	/**
	 * 终端标识
	 */
	private final String clientId;
	
	public ClientEventAdapter(Message message, Client client) {
		this(Map.of(), message, client);
	}
	
	public ClientEventAdapter(Map<?, ?> body, Message message, Client client) {
		super(body, message, client);
		this.client = client;
		this.clientId = client.clientId();
	}
	
}

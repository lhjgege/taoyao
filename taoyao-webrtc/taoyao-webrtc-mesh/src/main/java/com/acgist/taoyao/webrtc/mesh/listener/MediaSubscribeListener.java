package com.acgist.taoyao.webrtc.mesh.listener;

import java.util.Map;

import com.acgist.taoyao.boot.model.Message;
import com.acgist.taoyao.signal.event.media.MediaSubscribeEvent;
import com.acgist.taoyao.signal.listener.MediaListenerAdapter;

/**
 * 订阅监听
 * 
 * @author acgist
 */
public class MediaSubscribeListener extends MediaListenerAdapter<MediaSubscribeEvent> {

	@Override
	public void onApplicationEvent(MediaSubscribeEvent event) {
		final String sn = event.getSn();
		final String to = event.getTo();
		final Message message = event.getMessage();
		final Map<String, Object> mergeBody = event.mergeBody();
		mergeBody.put("from", sn);
		this.clientSessionManager.unicast(to, message);
	}

}

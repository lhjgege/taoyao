package com.acgist.taoyao.boot.model;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.acgist.taoyao.boot.utils.JSONUtils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 消息
 * 接口、信令、媒体信令通用消息模型
 * 
 * @author acgist
 */
@Getter
@Setter
@Schema(title = "消息", description = "消息")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 状态编码
	 */
	@Schema(title = "状态编码", description = "状态编码")
	private String code;
	/**
	 * 状态描述
	 */
	@Schema(title = "状态描述", description = "状态描述")
	private String message;
	/**
	 * 消息头部
	 */
	@Schema(title = "消息头部", description = "消息头部")
	private Header header;
	/**
	 * 消息主体
	 */
	@Schema(title = "消息主体", description = "消息主体")
	private Object body;
	
	/**
	 * @param code 状态编码
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * @param code 状态编码
	 */
	public void setCode(MessageCode code) {
		this.setCode(code, null);
	}
	
	/**
	 * @param code 状态编码
	 * @param message 状态描述
	 * 
	 * @return this
	 */
	public Message setCode(MessageCode code, String message) {
		this.code = code.getCode();
		this.message = StringUtils.isEmpty(message) ? code.getMessage() : message;
		return this;
	}

	/**
	 * @return 成功消息
	 */
	public static final Message success() {
		return success(null);
	}

	/**
	 * @param body 消息主体
	 * 
	 * @return 成功消息
	 */
	public static final Message success(Object body) {
		final Message message = new Message();
		message.setCode(MessageCode.CODE_0000, null);
		message.body = body;
		return message;
	}

	/**
	 * @return 失败消息
	 */
	public static final Message fail() {
		return fail(null, null, null);
	}

	/**
	 * @param code 状态编码
	 * 
	 * @return 失败消息
	 */
	public static final Message fail(MessageCode code) {
		return fail(code, null, null);
	}

	/**
	 * @param code 状态编码
	 * @param body 消息主体
	 * 
	 * @return 失败消息
	 */
	public static final Message fail(MessageCode code, Object body) {
		return fail(code, null, body);
	}
	
	/**
	 * @param message 状态描述
	 * 
	 * @return 失败消息
	 */
	public static final Message fail(String message) {
		return fail(null, message, null);
	}
	
	/**
	 * @param message 状态描述
	 * @param body 消息主体
	 * 
	 * @return 失败消息
	 */
	public static final Message fail(String message, Object body) {
		return fail(null, message, body);
	}
	
	/**
	 * @param code 状态编码
	 * @param message 状态描述
	 * 
	 * @return 失败消息
	 */
	public static final Message fail(MessageCode code, String message) {
		return fail(code, message, null);
	}

	/**
	 * @param code 状态编码
	 * @param message 状态描述
	 * @param body 消息主体
	 * 
	 * @return 失败消息
	 */
	public static final Message fail(MessageCode code, String message, Object body) {
		final Message failMessage = new Message();
		failMessage.setCode(code == null ? MessageCode.CODE_9999 : code, message);
		failMessage.body = body;
		return failMessage;
	}

	@Override
	public Message clone() {
		return new Message(this.code, this.message, this.header.clone(), this.body);
	}
	
	/**
	 * 克隆消息设置Map消息主体
	 * 
	 * @return 克隆消息
	 */
	public Message CloneWithMapBody() {
	    final Message message = this.clone();
	    message.setBody(new HashMap<>());
	    return message;
	}
	
	/**
	 * 克隆消息排除消息主体
	 * 
	 * @return 克隆消息
	 */
	public Message cloneWidthoutBody() {
		final Message message = this.clone();
		message.setBody(null);
		return message;
	}
	
	@Override
	public String toString() {
		return JSONUtils.toJSON(this);
	}
	
}

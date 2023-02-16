package com.acgist.taoyao.signal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.acgist.taoyao.boot.model.Message;
import com.acgist.taoyao.boot.property.SecurityProperties;
import com.acgist.taoyao.signal.client.Client;
import com.acgist.taoyao.signal.protocol.Protocol;
import com.acgist.taoyao.signal.service.SecurityService;

public class SecurityServiceImpl implements SecurityService {

	@Autowired
	private SecurityProperties securityProperties;
	
	@Override
	public boolean authenticate(String username, String password) {
		if(
			Boolean.TRUE.equals(this.securityProperties.getEnabled()) &&
			StringUtils.equals(this.securityProperties.getUsername(), username) &&
			StringUtils.equals(this.securityProperties.getPassword(), password)
		) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean authenticate(Message message, Client client, Protocol protocol) {
	    return client.authorized() && protocol.authenticate(message);
	}

}

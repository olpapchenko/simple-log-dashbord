package com.papchenko.logwebdashbord.service.impl;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogAgentSessionHandler extends StompSessionHandlerAdapter {

	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
		super.handleException(session, command, headers, payload, exception);
		log.error("Exception occurred during watching file", exception);
	}
}

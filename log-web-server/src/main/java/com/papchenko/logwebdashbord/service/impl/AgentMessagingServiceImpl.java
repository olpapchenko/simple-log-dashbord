package com.papchenko.logwebdashbord.service.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.papchenko.logwebdashbord.dto.LogSourceUpdateDto;
import com.papchenko.logwebdashbord.entity.WatchFileEntity;
import com.papchenko.logwebdashbord.repository.WatchFileRepository;
import com.papchenko.logwebdashbord.service.AgentMessagingService;
import com.papchenko.logwebdashbord.service.LogContentAlert;
import com.papchenko.logwebdashbord.service.Severity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AgentMessagingServiceImpl implements AgentMessagingService {
	private static final String TOPIC_PATTERN = "/topic/change/%s";
	private Map<Long, ListenableFuture<StompSession>> agentIdToStompSession = new ConcurrentHashMap<>();

	@Autowired
	private List<LogContentAlert> logContentAlerts;

	@Autowired
	private WatchFileRepository watchFileRepository;

	@Override
	public void connectAgent(Long agentId, String url) {
		log.debug("connect new agent agent id {}, agent url {}", agentId, url);
		WebSocketClient webSocketClient = new StandardWebSocketClient();

		WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
		stompClient.setTaskScheduler(new ConcurrentTaskScheduler());

		ListenableFuture<StompSession> stompSessionFuture = stompClient.connect(url, new LogAgentSessionHandler());
		agentIdToStompSession.put(agentId, stompSessionFuture);
	}

	@Override
	public void disconnect(Long agentId) {
		StompSession stompSession = null;
		try {
			stompSession = agentIdToStompSession.get(agentId).get();
		}
		catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		stompSession.disconnect();
		log.debug("agent disconnected {}", agentId);
	}

	@Override
	public void watchFile(Long agentId, String logFileKey) {
		ListenableFuture<StompSession> stompSessionListenableFuture = agentIdToStompSession.get(agentId);

		StompSession stompSession = null;
		try {
			stompSession = stompSessionListenableFuture.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		stompSession.subscribe(String.format(TOPIC_PATTERN, logFileKey),
				new StompFrameHandler() {
					@Override
					public Type getPayloadType(StompHeaders stompHeaders) {
						return LogSourceUpdateDto.class;
					}

					@Override
					public void handleFrame(StompHeaders stompHeaders, Object o) {
						LogSourceUpdateDto updateDto = (LogSourceUpdateDto) o;
						runAlerts(updateDto);
					}
				});
	}

	@Transactional
	public void runAlerts(LogSourceUpdateDto logSourceUpdateDto) {
		WatchFileEntity watchFileEntity = watchFileRepository.findOneByKey(logSourceUpdateDto.getKey());
		Optional<Integer> maxSeverity = logContentAlerts
				.stream()
				.map(logContentAlert -> logContentAlert.process(watchFileEntity.getId(), logSourceUpdateDto.getStrings()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(Severity::ordinal)
				.max(Integer::compareTo);

		Severity severity1 = maxSeverity.map(severity -> {
			return Severity.values()[severity];
		}).orElseGet(() -> null);

		watchFileEntity.setSeverity(severity1);
 	}
}

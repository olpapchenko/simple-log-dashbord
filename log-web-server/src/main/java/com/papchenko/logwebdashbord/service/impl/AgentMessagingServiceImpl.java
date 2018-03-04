package com.papchenko.logwebdashbord.service.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.papchenko.logwebdashbord.repository.LogSourceRepository;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.papchenko.logwebdashbord.dto.LogSourceUpdateDto;
import com.papchenko.logwebdashbord.entity.LogSourceEntity;
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

	@Autowired
	private ObjectFactory<WebSocketStompClient> webSocketStompClientObjectFactory;

	@Autowired
	private LogSourceRepository logSourceRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public void connectWithLogSource(Long logSourceId) {

		LogSourceEntity logSource = logSourceRepository.findOne(logSourceId);
		log.debug("connect new agent agent id {}, agent url {}", logSourceId, logSource.getUrl());
		WebSocketStompClient stompClient = webSocketStompClientObjectFactory.getObject();

		ListenableFuture<StompSession> stompSessionFuture = stompClient.connect(logSource.getUrl(),
				new LogAgentSessionHandler());
		agentIdToStompSession.put(logSourceId, stompSessionFuture);
	}

	@Override
	public void disconnect(Long logAgentId) {
		getStompSession(logAgentId).disconnect();
		log.debug("agent disconnected {}", logAgentId);
	}

	@Override
	public void watchFile(Long logSourceId, Long watchFileId) {
		StompSession stompSession = getStompSession(logSourceId);

		LogSourceEntity logSource = logSourceRepository.findOne(logSourceId);
		WatchFileEntity watchFile = watchFileRepository.findOne(watchFileId);

		ResponseEntity<String> registerForWatchingResponse = restTemplate.postForEntity(logSource.getUrl() + "/watch",
				watchFile.getPath(), String.class);
		//todo throw exception if response code is non 200
		String logFileKey = registerForWatchingResponse.getBody();

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
						sendNotificationLogChangeConsumed(logSource.getUrl(), logFileKey);
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

		Severity maxSeverityEnum = maxSeverity.map(severity -> Severity.values()[severity]).orElseGet(() -> null);

		watchFileEntity.setSeverity(maxSeverityEnum);
 	}

	private StompSession getStompSession(Long logSourceId) {
		try {
			return agentIdToStompSession.get(logSourceId).get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendNotificationLogChangeConsumed(String url, String logFileKey) {
		restTemplate.postForLocation(url + "/{key}", null,  logFileKey);
	}
}

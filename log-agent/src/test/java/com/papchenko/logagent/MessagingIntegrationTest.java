package com.papchenko.logagent;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
 import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.papchenko.logagent.dto.LogSourceUpdateDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class MessagingIntegrationTest {

	public static final int TIMEOUT = 2;

	@LocalServerPort
	private int port;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	private WebSocketStompClient socketStompClient;

	private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

	private AtomicReference<List<String>> strings = new AtomicReference<>();

	@Before
	public void setup() {
		log.info("test setup");
		Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
		List<Transport> transports = Collections.singletonList(webSocketTransport);

		SockJsClient sockJsClient = new SockJsClient(transports);

		this.socketStompClient = new WebSocketStompClient(sockJsClient);
		this.socketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@After
	public void clean() throws InterruptedException {
		Thread.sleep(500);
	}

	@Test
	public void testConnect() throws InterruptedException, ExecutionException {

		CountDownLatch countDownLatch = new CountDownLatch(1);

		ListenableFuture<StompSession> stompSession = this.socketStompClient.connect("ws://localhost:{port}/messaging/",
				this.headers,
				new SessionHandler(countDownLatch), this.port);


		if (!countDownLatch.await(TIMEOUT, TimeUnit.SECONDS)) {
			stompSession.wait(1000);
			stompSession.get().disconnect();
			throw new AssertionError("Connection was not established");
		}

		stompSession.get().disconnect();
	}

	@Test
	public void testClientNotifiedOnFileChange() throws IOException, InterruptedException, ExecutionException {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		String key = sendFileWatchRequest();

		ListenableFuture<StompSession> session = this.socketStompClient.connect("ws://localhost:{port}/messaging/",
				this.headers, new SessionHandler(countDownLatch, key), this.port);

		if (!countDownLatch.await(TIMEOUT, TimeUnit.SECONDS)) {
			alertConnectionWasNotEstablished();
		}

		List<String> expected = new ArrayList<>();
		expected.addAll(Arrays.asList("1", "2", "3"));

		writeToFile(expected);

		Thread.sleep(1500);

		assertTrue(expected.containsAll(strings.get()));
		session.get().disconnect();
	}

	@Test
	public void testClientNotifiedMultipleFileWrites() throws InterruptedException, ExecutionException {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		String key = sendFileWatchRequest();

		ListenableFuture<StompSession> session = this.socketStompClient.connect("ws://localhost:{port}/messaging/",
				this.headers, new SessionHandler(countDownLatch, key), this.port);

 		List<String> expectedLines = new ArrayList<>();

		if (!countDownLatch.await(TIMEOUT, TimeUnit.SECONDS)) {
			alertConnectionWasNotEstablished();
		}


		IntStream.range(1, 200).forEach(value -> {
			try {
				String s = String.valueOf(value);
				expectedLines.add(s);
				writeToFile(Arrays.asList(s));
				log.info("write to file");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		Thread.sleep(1500);

		assertTrue(expectedLines.containsAll(strings.get()));
		session.get().disconnect();
	}

	private void alertConnectionWasNotEstablished() {
		log.error("Connection was not established");
		throw new AssertionError("Connection was not established");
	}

	private void writeToFile(List<String> expected) throws IOException {
		Files.write(TestUtils.getTextFilePath1(), expected, StandardOpenOption.APPEND);
		log.info("write to file");
	}

	private String sendFileWatchRequest() {
		RestTemplate resetTemplate = restTemplateBuilder.build();
		log.info("send file watch request");
		String key = resetTemplate.postForObject("http://localhost:{port}/watch", TestUtils.getTextFilePath1().toString(), String.class, this.port);
		log.info("received file watch response with key {}", key);
		return key;
	}

	private void notifyFileConsumed(String key) {
		RestTemplate restTemplate = restTemplateBuilder.build();
		restTemplate.postForLocation("http://localhost:{port}/watch/{key}", null, this.port, key);
		log.info("send file change consumed notification");
	}

	private class SessionHandler extends StompSessionHandlerAdapter {
		private CountDownLatch countDownLatch;
		private String subscribeKey;

		public  SessionHandler(CountDownLatch countDownLatch) {
			this.countDownLatch = countDownLatch;
		}

		public  SessionHandler(CountDownLatch countDownLatch, String subscribeKey) {
			this.countDownLatch = countDownLatch;
			this.subscribeKey = subscribeKey;
		}

		@Override
		public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
			countDownLatch.countDown();
			log.info("connected to messaging server");

			if (Objects.nonNull(subscribeKey)) {
				session.subscribe("/topic/change/" + subscribeKey,
						new StompFrameHandler() {
							@Override
							public Type getPayloadType(StompHeaders stompHeaders) {
								return LogSourceUpdateDto.class;
							}

							@Override
							public void handleFrame(StompHeaders stompHeaders, Object o) {
								log.info("handle frame");
								LogSourceUpdateDto update = (LogSourceUpdateDto) o;
								strings.set(update.getStrings());
								notifyFileConsumed(subscribeKey);
								countDownLatch.countDown();
							}
						});
			}
		}

		@Override
		public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
				Throwable exception) {
			countDownLatch.countDown();
			exception.printStackTrace();
			super.handleException(session, command, headers, payload, exception);
		}

		@Override
		public void handleTransportError(StompSession session, Throwable exception) {
			countDownLatch.countDown();
			exception.printStackTrace();
			super.handleTransportError(session, exception);
		}
	}
}

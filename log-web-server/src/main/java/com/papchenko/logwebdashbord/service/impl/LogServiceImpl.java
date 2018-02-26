package com.papchenko.logwebdashbord.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import com.papchenko.logwebdashbord.entity.WatchFileEntity;
import com.papchenko.logwebdashbord.repository.LogAgentRepository;
import com.papchenko.logwebdashbord.repository.WatchFileRepository;
import com.papchenko.logwebdashbord.service.AgentMessagingService;
import com.papchenko.logwebdashbord.service.LogService;

@Service
public class LogServiceImpl implements LogService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LogAgentRepository logAgentRepository;

	@Autowired
	private WatchFileRepository watchFileRepository;

	@Autowired
	private AgentMessagingService agentMessagingService;

	//todo remove response entity from service
	@Override
	@Transactional
	public 	ResponseEntity<String> watchFile(Long logAgentId, String path, String name) {
		LogSourceEntity logSource = logAgentRepository.findOne(logAgentId);

		String logSourceUrl = logSource.getUrl();
		ResponseEntity<String> fileSourceResponse = restTemplate.postForEntity(logSourceUrl + "/watch", path, String.class);

		if (!fileSourceResponse.getStatusCode().equals(HttpStatus.OK)) {
			return fileSourceResponse;
		}



		WatchFileEntity watchFileEntity = new WatchFileEntity();

		watchFileEntity.setPath(path);
		watchFileEntity.setName(name);
		watchFileEntity.setLogSourceEntity(logSource);
		watchFileEntity.setKey(fileSourceResponse.getBody());

		watchFileRepository.save(watchFileEntity);

		agentMessagingService

		return fileSourceResponse;
	}

	@Override
	@Transactional
	public void removeWatchFile(Long id) {
		watchFileRepository.delete(id);
	}



}

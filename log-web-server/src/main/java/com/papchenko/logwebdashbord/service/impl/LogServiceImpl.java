package com.papchenko.logwebdashbord.service.impl;

import com.papchenko.logwebdashbord.dto.FileLogDto;
import com.papchenko.logwebdashbord.utils.Transformer;
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

import java.util.List;
import java.util.stream.Collectors;

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
	public 	ResponseEntity<String> watchFile(FileLogDto fileLogDto) {
		LogSourceEntity logSource = logAgentRepository.findOne(fileLogDto.getLogSourceId());

		String logSourceUrl = logSource.getUrl();
		ResponseEntity<String> fileSourceResponse = restTemplate.postForEntity(logSourceUrl + "/watch", fileLogDto.getPath(), String.class);

		if (!fileSourceResponse.getStatusCode().equals(HttpStatus.OK)) {
			return fileSourceResponse;
		}

		WatchFileEntity watchFileEntity = new WatchFileEntity();

		watchFileEntity.setPath(fileLogDto.getPath());
		watchFileEntity.setName(fileLogDto.getName());
		watchFileEntity.setLogSourceEntity(logSource);
		watchFileEntity.setKey(fileSourceResponse.getBody());

		watchFileRepository.save(watchFileEntity);

		return fileSourceResponse;
	}

	@Override
	@Transactional(readOnly = true)
	public List<FileLogDto> getAllWatchFiles(Long logAgentId) {
		LogSourceEntity logAgent = logAgentRepository.findOne(logAgentId);
		List<WatchFileEntity> allByLogSourceEntity = watchFileRepository.findAllByLogSourceEntity(logAgent);

		return allByLogSourceEntity
				.stream()
				.map(Transformer::toFileLogDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void removeWatchFile(Long id) {
		watchFileRepository.delete(id);
	}

}

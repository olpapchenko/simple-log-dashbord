package com.papchenko.logwebdashbord.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.papchenko.logwebdashbord.entity.TextAlertEntity;
import com.papchenko.logwebdashbord.repository.WatchFileRepository;
import com.papchenko.logwebdashbord.service.LogContentAlert;
import com.papchenko.logwebdashbord.service.Severity;

@Component
public class LogContainsTextAlert implements LogContentAlert {

	@Autowired
	private WatchFileRepository watchFileRepository;

	@Override
	@Transactional
	public Optional<Severity> process(Long logId, List<String> logContent) {
		return watchFileRepository
				.findOne(logId)
				.getLogSourceEntity()
				.getTextAlertEntities()
				.stream()
				.filter(textAlertEntity -> contains(logContent, textAlertEntity.getText()))
				.findFirst()
				.map(TextAlertEntity::getSeverity);
	}

	private boolean contains(List<String> strings, String entry) {
		return strings
				.stream()
				.anyMatch(s -> s.contains(entry));
	}
}

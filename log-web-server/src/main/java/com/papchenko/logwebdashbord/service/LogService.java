package com.papchenko.logwebdashbord.service;

import org.springframework.http.ResponseEntity;

public interface LogService {
	ResponseEntity<String> watchFile(Long logAgentId, String path, String name);

	void removeWatchFile(Long id);
}

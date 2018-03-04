package com.papchenko.logwebdashbord.service;

import com.papchenko.logwebdashbord.dto.FileLogDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LogService {
	ResponseEntity<String> watchFile(FileLogDto file);

	List<FileLogDto> getAllWatchFiles(Long logAgentId);

	void removeWatchFile(Long id);
}

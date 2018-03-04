package com.papchenko.logwebdashbord.web;

import com.papchenko.logwebdashbord.dto.FileLogDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.papchenko.logwebdashbord.service.LogService;

@RestController
@RequestMapping("logsource")
public class LogSourceResource {

	@Autowired
	private LogService logService;

	@PostMapping
	public ResponseEntity<String> watchFile(@RequestBody FileLogDto fileLogSource) {
		return logService.watchFile(fileLogSource);
	}

	@DeleteMapping("/{watchFileId}")
	public void removeWatchFile( @PathVariable("watchSourceId") Long watchFileId) {
		logService.removeWatchFile(watchFileId);
	}



}

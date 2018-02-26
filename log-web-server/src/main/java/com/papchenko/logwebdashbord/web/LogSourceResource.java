package com.papchenko.logwebdashbord.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.papchenko.logwebdashbord.service.LogService;

@RestController
@RequestMapping("logsource")
public class LogSourceResource {

	@Autowired
	private LogService logService;

	@PostMapping("/{logAgentId}/{path}/{name}")
	public ResponseEntity<String> watchFile(@PathVariable("logAgentId") Long logAgentId,
			@PathVariable("path") String path,
			@PathVariable("name") String name) {
		return logService.watchFile(logAgentId, path, name);
	}

	@DeleteMapping("/{watchFileId}")
	public void removeWatchFile( @PathVariable("watchSourceId") Long watchFileId) {
		logService.removeWatchFile(watchFileId);
	}



}

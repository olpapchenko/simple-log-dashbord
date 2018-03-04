package com.papchenko.logwebdashbord.web;

import com.papchenko.logwebdashbord.dto.FileLogDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.papchenko.logwebdashbord.service.LogService;

@RestController
@RequestMapping("file")
public class LogFileResource {

	@Autowired
	private LogService logService;

	@PostMapping
	public void watchFile(@RequestBody FileLogDto fileLogSource) {
	  	logService.saveWatchFileInfo(fileLogSource);
	}

	@DeleteMapping("/{watchFileId}")
	public void removeWatchFile( @PathVariable("watchSourceId") Long watchFileId) {
		logService.removeWatchFile(watchFileId);
	}
}

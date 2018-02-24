package com.papchenko.logwebdashbord.web;

import com.papchenko.logwebdashbord.dto.LogSourceDto;
import com.papchenko.logwebdashbord.service.LogSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@Controller
@RequestMapping("logsource")
public class LogAgentResource {

    @Autowired
    private LogSourceService logSourceService;

    @PostMapping
    public void save(LogSourceDto logSourceDto) {
        logSourceService.save(logSourceDto);
    }

    @PutMapping
    public void update(LogSourceDto logSourceDto) {
        logSourceService.update(logSourceDto);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathParam("id") Long id) {
        logSourceService.remove(id);
    }

    @GetMapping(path = "/all")
    public List<LogSourceDto> all() {
        return logSourceService.getAllLog();
    }
}

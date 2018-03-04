package com.papchenko.logwebdashbord.service;

import com.papchenko.logwebdashbord.dto.LogSourceDto;

import java.util.List;

public interface LogSourceService {
    void save(LogSourceDto logSourceDto);

    void update(LogSourceDto logSourceDto);

    List<LogSourceDto> getAllLogSources();

    void remove(Long id);
}

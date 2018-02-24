package com.papchenko.logwebdashbord.service;

import com.papchenko.logwebdashbord.dto.LogSourceDto;

import java.util.List;

public interface LogSource {
    void save(LogSourceDto logSourceDto);

    void update(LogSourceDto logSourceDto);

    List<LogSourceDto> getAllLogResources();

    void removeResource(Long id);
}

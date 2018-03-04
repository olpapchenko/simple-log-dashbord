package com.papchenko.logwebdashbord.utils;

import com.papchenko.logwebdashbord.dto.FileLogDto;
import com.papchenko.logwebdashbord.dto.LogSourceDto;
import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import com.papchenko.logwebdashbord.entity.WatchFileEntity;

public final class Transformer {

    private Transformer() {
    }

    public static LogSourceEntity toLogSourceEntity(LogSourceDto logSourceDto) {
        LogSourceEntity logSourceEntity = new LogSourceEntity();

        logSourceEntity.setId(logSourceDto.getId());
        logSourceEntity.setName(logSourceDto.getName());
        logSourceEntity.setUrl(logSourceDto.getUrl());
        logSourceEntity.setStatus(logSourceDto.isHealth());

        return logSourceEntity;
    }

    public static LogSourceDto toLogSourceDto(LogSourceEntity logSourceEntity) {
        LogSourceDto logSourceDto = new LogSourceDto();

        logSourceDto.setId(logSourceEntity.getId());
        logSourceDto.setName(logSourceEntity.getName());
        logSourceDto.setHealth(logSourceEntity.isStatus());
        logSourceDto.setUrl(logSourceEntity.getUrl());

        return logSourceDto;
    }

    public static FileLogDto toFileLogDto(WatchFileEntity watchFileEntity) {
        FileLogDto dto = new FileLogDto();

        dto.setName(watchFileEntity.getName());
        dto.setLogSourceId(watchFileEntity.getLogSourceEntity().getId());
        dto.setPath(watchFileEntity.getPath());

        return dto;
    }
}

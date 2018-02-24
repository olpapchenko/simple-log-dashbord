package com.papchenko.logwebdashbord.utils;

import com.papchenko.logwebdashbord.dto.LogSourceDto;
import com.papchenko.logwebdashbord.entity.LogSourceEntity;

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
}

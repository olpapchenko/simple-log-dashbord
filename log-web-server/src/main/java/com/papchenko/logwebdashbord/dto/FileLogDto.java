package com.papchenko.logwebdashbord.dto;

import lombok.Data;


@Data
public class FileLogDto {
    private String path;
    private String name;
    private Long logSourceId;
}

package com.papchenko.logwebdashbord.dto;

import lombok.Data;

@Data
public class LogSourceDto {
    private Long id;

    private String url;
    private String name;
    private boolean health;
}

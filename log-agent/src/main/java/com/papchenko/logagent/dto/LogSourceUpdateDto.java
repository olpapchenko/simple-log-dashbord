package com.papchenko.logagent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LogSourceUpdateDto {
    public LogSourceUpdateDto() {
    }

    private String id;
    private List<String> strings;
}

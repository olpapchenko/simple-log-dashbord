package com.papchenko.logwebdashbord.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogSourceUpdateDto {
	public LogSourceUpdateDto() {
	}

	private String key;
	private List<String> strings;
}

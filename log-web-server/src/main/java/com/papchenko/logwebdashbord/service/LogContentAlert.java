package com.papchenko.logwebdashbord.service;

import java.util.List;
import java.util.Optional;

public interface LogContentAlert {
	Optional<Severity> process(Long logId, List<String> logContent);
}

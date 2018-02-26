package com.papchenko.logwebdashbord.repository;

import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import org.springframework.data.repository.CrudRepository;

public interface LogAgentRepository extends CrudRepository<LogSourceEntity, Long> {
}

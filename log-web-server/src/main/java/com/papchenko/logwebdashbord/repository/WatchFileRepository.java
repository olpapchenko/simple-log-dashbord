package com.papchenko.logwebdashbord.repository;

import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import org.springframework.data.repository.CrudRepository;

import com.papchenko.logwebdashbord.entity.WatchFileEntity;

import java.util.List;

public interface WatchFileRepository extends CrudRepository<WatchFileEntity, Long>{
	WatchFileEntity findOneByKey(String key);
	List<WatchFileEntity> findAllByLogSourceEntity(LogSourceEntity logSourceEntity);
}

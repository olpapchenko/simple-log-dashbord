package com.papchenko.logwebdashbord.repository;

import org.springframework.data.repository.CrudRepository;

import com.papchenko.logwebdashbord.entity.WatchFileEntity;

public interface WatchFileRepository extends CrudRepository<WatchFileEntity, Long>{
	WatchFileEntity findOneByKey(String key);
}

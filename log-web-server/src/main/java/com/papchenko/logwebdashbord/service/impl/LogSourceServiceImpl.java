package com.papchenko.logwebdashbord.service.impl;

import com.papchenko.logwebdashbord.dto.LogSourceDto;
import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import com.papchenko.logwebdashbord.repository.LogSourceRepository;
import com.papchenko.logwebdashbord.service.LogSourceService;
import com.papchenko.logwebdashbord.utils.Transformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class LogSourceServiceImpl implements LogSourceService {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private LogSourceRepository logSourceRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${health.check.interval:1000}")
    private long healthCheckInterval;

    @Override
    @Transactional
    public void save(LogSourceDto logSourceDto) {
        log.debug("saving new log source {}", logSourceDto.getUrl());
        logSourceRepository.save(Transformer.toLogSourceEntity(logSourceDto));
    }

    @Override
    @Transactional
    public void update(LogSourceDto logSourceDto) {
        log.debug("updating new log source {}", logSourceDto.getUrl());
        logSourceRepository.save(Transformer.toLogSourceEntity(logSourceDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogSourceDto> getAllLogSources() {

        List<LogSourceDto> logSources = new ArrayList<>();
        logSourceRepository.findAll().forEach(logSourceEntity -> {
            logSources.add(Transformer.toLogSourceDto(logSourceEntity));
        });

        log.debug("getting log sources count {}", logSources.size());
        return logSources;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        log.debug("removing log source with id {}", id);
        logSourceRepository.delete(id);
    }

    @Scheduled(fixedDelay = 500L)
    @Transactional
    private void startHealthCheckLoop() {
            log.debug("running health check");
            Iterable<LogSourceEntity> all = logSourceRepository.findAll();

            all.forEach(logSourceEntity -> {
                String url = logSourceEntity.getUrl();
                Boolean status = false;

                try {
                    status = restTemplate.getForObject(url + "/status", Boolean.class);
                } catch (RestClientException e) {
                    status = false;
                    log.info("agent is not working {}", logSourceEntity.getName());
                }

                logSourceEntity.setStatus(status);
                logSourceRepository.save(logSourceEntity);
            });
    }
}

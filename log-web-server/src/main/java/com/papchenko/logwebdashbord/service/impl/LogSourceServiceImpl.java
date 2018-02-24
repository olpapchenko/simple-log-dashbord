package com.papchenko.logwebdashbord.service.impl;

import com.papchenko.logwebdashbord.dto.LogSourceDto;
import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import com.papchenko.logwebdashbord.repository.LogSourceRepository;
import com.papchenko.logwebdashbord.service.LogSourceService;
import com.papchenko.logwebdashbord.utils.Transformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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

    @Value("health.check.interval:1000")
    private Long healthCheckInterval;

    @Override
    public void save(LogSourceDto logSourceDto) {
        logSourceRepository.save(Transformer.toLogSourceEntity(logSourceDto));
    }

    @Override
    public void update(LogSourceDto logSourceDto) {
        logSourceRepository.save(Transformer.toLogSourceEntity(logSourceDto));
    }

    @Override
    public List<LogSourceDto> getAllLog() {

        List<LogSourceDto> result = new ArrayList<>();
        logSourceRepository.findAll().forEach(logSourceEntity -> {
            result.add(Transformer.toLogSourceDto(logSourceEntity));
        });

        return result;
    }

    @Override
    public void remove(Long id) {
        logSourceRepository.delete(id);
    }

    @PostConstruct
    private void startHelthCheckLoop() {
        executorService.execute(() -> {

            while(true) {
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

                try {
                    Thread.sleep(healthCheckInterval);
                } catch (InterruptedException e) {
                    log.error("error occured during health check");
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

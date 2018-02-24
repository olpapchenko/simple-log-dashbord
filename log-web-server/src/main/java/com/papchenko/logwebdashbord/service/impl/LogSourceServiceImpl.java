package com.papchenko.logwebdashbord.service.impl;

import com.papchenko.logwebdashbord.dto.LogSourceDto;
import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import com.papchenko.logwebdashbord.repository.LogSourceRepository;
import com.papchenko.logwebdashbord.service.LogSource;
import com.papchenko.logwebdashbord.utils.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LogSourceServiceImpl implements LogSource {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private LogSourceRepository logSourceRepository;

    @Override
    public void save(LogSourceDto logSourceDto) {
        logSourceRepository.save(Transformer.toLogSourceEntity(logSourceDto));
    }

    @Override
    public void update(LogSourceDto logSourceDto) {
        logSourceRepository.save(Transformer.toLogSourceEntity(logSourceDto));
    }

    @Override
    public List<LogSourceDto> getAllLogResources() {

        List<LogSourceDto> result = new ArrayList<>();
        logSourceRepository.findAll().forEach(logSourceEntity -> {
            result.add(Transformer.toLogSourceDto(logSourceEntity));
        });

        return result;
    }

    @Override
    public void removeResource(Long id) {
        logSourceRepository.delete(id);
    }

    @PostConstruct
    private void startHelthCheckLoop() {
        executorService.execute(() -> {
            Iterable<LogSourceEntity> all = logSourceRepository.findAll();


        });
    }
}

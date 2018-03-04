package com.papchenko.logwebdashbord;

import com.papchenko.logwebdashbord.entity.LogSourceEntity;
import com.papchenko.logwebdashbord.entity.TextAlertEntity;
import com.papchenko.logwebdashbord.entity.WatchFileEntity;
import com.papchenko.logwebdashbord.repository.WatchFileRepository;
import com.papchenko.logwebdashbord.service.Severity;
import com.papchenko.logwebdashbord.service.impl.LogContainsTextAlert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogContainsTextAlertTest {

    private static final String TEST_TEXT_TO_WATCH = "test text";
    private static final List<String> TEST_LOG_TEXT_WITH_ALERT = Arrays.asList("some", TEST_TEXT_TO_WATCH, "test2");
    private static final List<String> TEST_LOG_TEXT_WITHOUT_ALERT = Arrays.asList("some", "test2");

    @Mock
    private WatchFileRepository watchFileRepository;

    @InjectMocks
    private LogContainsTextAlert logContainsTextAlert;

    @Test
    public void testAlertIsTriggeredWhenTextIsPresented() {
        //given
        WatchFileEntity watchFileEntity = new WatchFileEntity();
        LogSourceEntity logSourceEntity = new LogSourceEntity();
        TextAlertEntity textAlertEntity = new TextAlertEntity();
        Severity expectedSeverity = Severity.MEDIUM;
        textAlertEntity.setSeverity(expectedSeverity);
        textAlertEntity.setText(TEST_TEXT_TO_WATCH);
        logSourceEntity.setTextAlertEntities(Arrays.asList(textAlertEntity));
        watchFileEntity.setLogSourceEntity(logSourceEntity);
        when(watchFileRepository.findOne(anyLong())).thenReturn(watchFileEntity);

        //when
        Optional<Severity> result = logContainsTextAlert.process(1L, TEST_LOG_TEXT_WITH_ALERT);

        //then
        assertTrue(result.isPresent());
        assertEquals(expectedSeverity, result.get());
    }

    @Test
    public void testAlertIsNotTriggeredWhenTextIsPresented() {
        //given
        WatchFileEntity watchFileEntity = new WatchFileEntity();
        LogSourceEntity logSourceEntity = new LogSourceEntity();
        TextAlertEntity textAlertEntity = new TextAlertEntity();
        Severity expectedSeverity = Severity.MEDIUM;
        textAlertEntity.setSeverity(expectedSeverity);
        textAlertEntity.setText(TEST_TEXT_TO_WATCH);
        logSourceEntity.setTextAlertEntities(Arrays.asList(textAlertEntity));
        watchFileEntity.setLogSourceEntity(logSourceEntity);
        when(watchFileRepository.findOne(anyLong())).thenReturn(watchFileEntity);

        //when
        Optional<Severity> result = logContainsTextAlert.process(1L, TEST_LOG_TEXT_WITHOUT_ALERT);

        //then
        assertFalse(result.isPresent());
    }
}

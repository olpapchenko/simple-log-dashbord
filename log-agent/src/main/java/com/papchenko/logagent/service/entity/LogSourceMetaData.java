package com.papchenko.logagent.service.entity;

import com.papchenko.logagent.utils.FilesUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class LogSourceMetaData {
    private long offset;
    private FileLogSource FileLogSource;

    public LogSourceMetaData(FileLogSource fileLogSource) {
        FileLogSource = fileLogSource;
        offset = FilesUtils.getFileLinesCount(fileLogSource.getLogPath());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LogSourceMetaData that = (LogSourceMetaData) o;
        return Objects.equals(getFileLogSource(), that.getFileLogSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getFileLogSource());
    }
}
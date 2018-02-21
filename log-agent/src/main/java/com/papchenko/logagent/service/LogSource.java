package com.papchenko.logagent.service;

import java.util.function.Consumer;

public interface LogSource<T> {
    /**
     * Returns next portion of text log data
     * @param offset - offset of data, starting from end od the start of data stream
     * @param size - number of lines returned
     * @return - log text dat
     */
    String getLogContent(String key, int offset, int size);


    /**
     * Adds log source for watching
     * @param logSource - log source to be watched
     */
    void addLogSource(T logSource);

    /**
     * Removes all log sources
     */
    void clearAll();

    /**
     * Clear watch for log source with provided log source key
     * @param key - log source key
     */
    void clear(String key);
}

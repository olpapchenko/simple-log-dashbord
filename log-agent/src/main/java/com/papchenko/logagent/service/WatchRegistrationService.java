package com.papchenko.logagent.service;

public interface WatchRegistrationService<T> {
    String registerWatchedFile(T registrationData);

    void notifyMessageConsumed(String logSourceId);
}

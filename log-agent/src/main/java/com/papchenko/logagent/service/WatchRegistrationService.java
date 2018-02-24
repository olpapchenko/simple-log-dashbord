package com.papchenko.logagent.service;

public interface WatchRegistrationService<T> {
    String registerNewWatchedFile(T registrationData);

    void notifyMessageConsumed(String logSourceId);
}

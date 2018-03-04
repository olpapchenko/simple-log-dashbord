package com.papchenko.logwebdashbord.service;

public interface AgentMessagingService {
	  void connectWithLogSource(Long logSourceId);

	  void disconnect(Long logSourceId);

	  void watchFile(Long logSourceId, Long watchFileId);
}

package com.papchenko.logwebdashbord.service;

public interface AgentMessagingService {
	  void connectAgent(Long agentId, String url);

	  void disconnect(Long agentId);

	  void watchFile(Long agentId, String logFileKey);
}

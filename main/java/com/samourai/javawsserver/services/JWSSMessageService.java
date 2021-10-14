package com.samourai.javawsserver.services;

import com.samourai.javawsserver.config.JWSSConfig;
import com.samourai.javawsserver.utils.JWSSUtils;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

// instanciate it with @Service
public abstract class JWSSMessageService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private SimpMessagingTemplate messagingTemplate;
  private TaskExecutor taskExecutor;
  private JWSSConfig config;

  @Autowired
  public JWSSMessageService(
      SimpMessagingTemplate messagingTemplate, TaskExecutor taskExecutor, JWSSConfig config) {
    this.messagingTemplate = messagingTemplate;
    this.taskExecutor = taskExecutor;
    this.config = config;
    messagingTemplate.setMessageConverter(new MappingJackson2MessageConverter());
  }

  public void sendPrivate(String username, Object payload) {
    sendPrivate(Arrays.asList(username), payload);
  }

  public void sendPrivate(Collection<String> usernames, Object payload) {
    if (log.isTraceEnabled()) {
      log.trace(
          "(>) "
              + String.join(",", usernames)
              + ": "
              + JWSSUtils.getInstance().toJsonString(payload));
    }
    usernames.forEach(
        username -> {
          taskExecutor.execute(
              () ->
                  messagingTemplate.convertAndSendToUser(
                      username, config.getPrefixUserReply(), payload, computeHeaders(payload)));
        });
  }

  public void send(String destination, Object payload) {
    if (log.isTraceEnabled()) {
      log.trace("(>>) " + destination + ": " + JWSSUtils.getInstance().toJsonString(payload));
    }
    messagingTemplate.convertAndSend(destination, payload, computeHeaders(payload));
  }

  protected Map<String, Object> computeHeaders(Object payload) {
    // to be overriden
    return new HashMap<>();
  }
}

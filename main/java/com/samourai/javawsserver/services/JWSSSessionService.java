package com.samourai.javawsserver.services;

import com.samourai.javawsserver.utils.JWSSMessageListener;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

// instanciate it with @Service
public abstract class JWSSSessionService {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private TaskExecutor taskExecutor;
  private List<JWSSMessageListener<String>> onDisconnectListeners;

  @Autowired
  public JWSSSessionService(TaskExecutor taskExecutor) {
    this.onDisconnectListeners = new ArrayList<>();
    this.taskExecutor = taskExecutor;
  }

  public void addOnDisconnectListener(JWSSMessageListener<String> listener) {
    onDisconnectListeners.add(listener);
  }

  public void onConnect(String username) {
    if (log.isTraceEnabled()) {
      log.trace("(<) " + username + " connect");
    }
  }

  public void onDisconnect(String username) {
    if (log.isTraceEnabled()) {
      log.trace("(<) " + username + ": disconnect");
    }
    // run in new thread for non-blocking websocket
    taskExecutor.execute(
        () -> {
          for (JWSSMessageListener<String> listener : onDisconnectListeners) {
            listener.onMessage(username);
          }
        });
  }
}

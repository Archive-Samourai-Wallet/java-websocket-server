package com.samourai.javawsserver.interceptors;

import com.sun.security.auth.UserPrincipal;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * Assign a principal for each websocket client. This is needed to be able to communicate with a
 * specific client.
 */
public class JWSSAssignPrincipalChannelInterceptor implements ChannelInterceptor {
  private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      if (log.isTraceEnabled()) {
        log.trace(
            "Assigning principal from sessionId: login="
                + (accessor.getLogin() != null ? accessor.getLogin() : "null")
                + ",sessionId="
                + accessor.getSessionId());
      }
      accessor.setUser(new UserPrincipal(accessor.getSessionId()));
    }
    return message;
  }
}

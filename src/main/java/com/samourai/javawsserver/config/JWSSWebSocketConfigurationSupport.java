package com.samourai.javawsserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samourai.javawsserver.interceptors.JWSSAssignPrincipalChannelInterceptor;
import com.samourai.javawsserver.interceptors.JWSSIpHandshakeInterceptor;
import com.samourai.javawsserver.services.JWSSSessionService;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/** Websocket configuration with STOMP. */
@EnableWebSocketMessageBroker
// instanciate it with @Configuration
public abstract class JWSSWebSocketConfigurationSupport
    extends WebSocketMessageBrokerConfigurationSupport implements WebSocketMessageBrokerConfigurer {
  private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private JWSSConfig config;
  private JWSSSessionService sessionService;

  public JWSSWebSocketConfigurationSupport(JWSSConfig config, JWSSSessionService sessionService) {
    this.config = config;
    this.sessionService = sessionService;
  }

  @Autowired private ObjectMapper objectMapper;

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
    super.configureWebSocketTransport(registry);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    super.configureClientInboundChannel(registration);
    registration.interceptors(new JWSSAssignPrincipalChannelInterceptor());
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    super.configureClientOutboundChannel(registration);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    super.addArgumentResolvers(argumentResolvers);
  }

  @Override
  public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    super.addReturnValueHandlers(returnValueHandlers);
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    if (log.isDebugEnabled()) {
      log.debug("+ STOMP endpoints: " + config.getWebsocketEndpoints());
    }
    // standard websocket
    registry
        .addEndpoint(config.getWebsocketEndpoints())
        .setAllowedOriginPatterns("*")
        .addInterceptors(new JWSSIpHandshakeInterceptor());

    // sockjs support
    registry
        .addEndpoint(config.getWebsocketEndpoints())
        .setAllowedOriginPatterns("*")
        .addInterceptors(new JWSSIpHandshakeInterceptor())
        .withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    //// registry.setPreservePublishOrder()
    // enable heartbeat (mandatory to detect client disconnect)
    ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
    te.setPoolSize(1);
    te.setThreadNamePrefix("wss-heartbeat-thread-");
    te.initialize();

    registry
        // .setApplicationDestinationPrefixes(config.getAppDestinationPrefix())
        .setUserDestinationPrefix(config.getPrefixUserPrivate())
        .enableSimpleBroker(config.getDestinationPrefix(), config.getPrefixUserReply())
        .setHeartbeatValue(config.getHeartbeatDelay())
        .setTaskScheduler(te);
  }

  @Override
  public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
    DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
    resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setObjectMapper(objectMapper);
    converter.setContentTypeResolver(resolver);
    messageConverters.add(converter);
    return false;
  }

  // listeners for logging purpose

  @EventListener
  public void handleSubscribeEvent(SessionSubscribeEvent event) {
    String username = event.getUser() != null ? event.getUser().getName() : "unknown";
    String message = event.getMessage() != null ? event.getMessage().toString() : "";
    if (log.isDebugEnabled()) {
      log.debug("(<) " + username + " subscribe: " + message);
    }
  }

  @EventListener
  public void handleConnectEvent(SessionConnectEvent event) {
    String username = event.getUser() != null ? event.getUser().getName() : "unknown";
    if (log.isDebugEnabled()) {
      log.debug("(<) " + username + " connect");
    }
    sessionService.onConnect(username);
  }

  @EventListener
  public void handleDisconnectEvent(SessionDisconnectEvent event) {
    String username = event.getUser() != null ? event.getUser().getName() : "unknown";
    if (log.isDebugEnabled()) {
      log.debug("(<) " + username + " disconnect");
    }
    sessionService.onDisconnect(username);
  }
}

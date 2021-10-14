package com.samourai.javawsserver.config;

import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

// instanciate it with @Configuration
public abstract class JWSSWebSocketSecurityConfig
    extends AbstractSecurityWebSocketMessageBrokerConfigurer {
  private JWSSConfig config;

  public JWSSWebSocketSecurityConfig(JWSSConfig config) {
    this.config = config;
  }

  @Override
  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
    super.configureInbound(messages);

    // allow websocket server endpoints
    messages
        .simpMessageDestMatchers(config.getWebsocketEndpoints())
        .permitAll()

        // deny any other messages (including client-to-client)
        .simpMessageDestMatchers("/**")
        .denyAll();
  }

  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }
}

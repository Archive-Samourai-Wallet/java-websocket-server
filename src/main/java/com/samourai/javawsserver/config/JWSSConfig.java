package com.samourai.javawsserver.config;

// instanciate it with @Bean
public class JWSSConfig {
  private static final long HEARTBEAT_DELAY = 20000;

  private String[] websocketEndpoints;
  private String prefixUserPrivate;
  private String prefixUserReply;
  private String appDestinationPrefix;
  private String destinationPrefix;
  private long[] heartbeatDelay;

  public JWSSConfig(
      String[] websocketEndpoints,
      String prefixUserPrivate,
      String appDestinationPrefix,
      String destinationPrefix,
      String prefixUserReply) {
    this.websocketEndpoints = websocketEndpoints;
    this.prefixUserPrivate = prefixUserPrivate;
    this.prefixUserReply = prefixUserReply;
    this.appDestinationPrefix = appDestinationPrefix;
    this.destinationPrefix = destinationPrefix;
    this.heartbeatDelay = new long[] {HEARTBEAT_DELAY, HEARTBEAT_DELAY};
  }

  public String[] getWebsocketEndpoints() {
    return websocketEndpoints;
  }

  public String getPrefixUserPrivate() {
    return prefixUserPrivate;
  }

  public String getPrefixUserReply() {
    return prefixUserReply;
  }

  public String getAppDestinationPrefix() {
    return appDestinationPrefix;
  }

  public String getDestinationPrefix() {
    return destinationPrefix;
  }

  public long[] getHeartbeatDelay() {
    return heartbeatDelay;
  }
}

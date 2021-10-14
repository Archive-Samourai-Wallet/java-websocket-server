package com.samourai.javawsserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWSSUtils {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static JWSSUtils instance;

  public JWSSUtils() {}

  public static JWSSUtils getInstance() {
    if (instance == null) {
      instance = new JWSSUtils();
    }
    return instance;
  }

  public String toJsonString(Object o) {
    try {
      return objectMapper.writeValueAsString(o);
    } catch (Exception var3) {
      log.error("", var3);
      return null;
    }
  }
}

package com.google.sps.utility;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public final class ApiKeyProvider {
  public static String get(String name) throws IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream stream = loader.getResourceAsStream("API_KEYS.json");
    if (stream == null) {
      throw new FileNotFoundException(
          "make sure your local keys are stored under src/main/resources/API_KEYS.json");
    }
    String rawJson = IOUtils.toString(stream, StandardCharsets.UTF_8);
    stream.close();
    Gson gson = new Gson();
    Type mapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> keys = gson.fromJson(rawJson, mapType);
    return keys.get(name);
  }
}

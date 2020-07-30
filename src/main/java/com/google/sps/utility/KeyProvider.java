// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

public final class KeyProvider {
  private String rawJson;

  /**
   * Constructor to create a KeyProvider instance to get keys from src/main/resources/KEYS.json
   *
   * @throws IOException
   */
  public KeyProvider() throws IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream stream = loader.getResourceAsStream("KEYS.json");
    if (stream == null) {
      throw new FileNotFoundException(
          "make sure your local keys are stored under src/main/resources/KEYS.json");
    }
    this.rawJson = IOUtils.toString(stream, StandardCharsets.UTF_8);
    stream.close();
  }

  /**
   * Constructor to create a mock KeyProvider instance for testing purposes
   *
   * @param rawJson represents the json content in a json file
   */
  public KeyProvider(String rawJson) {
    this.rawJson = rawJson;
  }

  /**
   * Obtains the value of a key from a json string
   *
   * @param key represents the key to obtain the corresponding value from
   * @return corresponding value of key
   * @throws IOException
   */
  public String getKey(String key) throws IOException {
    Gson gson = new Gson();
    Type mapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> keys = gson.fromJson(rawJson, mapType);
    return keys.get(key);
  }
}

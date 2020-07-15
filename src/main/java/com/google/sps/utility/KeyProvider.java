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

/** Class to interact with the Secret Manager API. */
public final class KeyProvider {

  /**
   * Gets the value of a key from the Secret Manager API.
   *
   * @param name A string representing the name of the key.
   * @return A string representing the value of stored under the given key.
   * @throws IOException
   */
  public static String getKey(String name) throws IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream stream = loader.getResourceAsStream("KEYS.json");
    if (stream == null) {
      throw new FileNotFoundException(
          "make sure your local keys are stored under src/main/resources/KEYS.json");
    }
    String rawJson = IOUtils.toString(stream, StandardCharsets.UTF_8);
    stream.close();
    Gson gson = new Gson();
    Type mapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> keys = gson.fromJson(rawJson, mapType);
    return keys.get(name);
  }

  /**
   * Gets the value of a key from the Secret Manager API with a specified loader.
   *
   * @param name A string representing the name of the key.
   * @param loader A ClassLoader to get resource from.
   * @return A string representing the value of stored under the given key.
   * @throws IOException
   */
  public static String getKey(String name, ClassLoader loader) throws IOException {
    InputStream stream = loader.getResourceAsStream("KEYS.json");
    if (stream == null) {
      throw new FileNotFoundException(
          "make sure your local keys are stored under src/main/resources/KEYS.json");
    }
    String rawJson = IOUtils.toString(stream, StandardCharsets.UTF_8);
    stream.close();
    Gson gson = new Gson();
    Type mapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> keys = gson.fromJson(rawJson, mapType);
    return keys.get(name);
  }
}

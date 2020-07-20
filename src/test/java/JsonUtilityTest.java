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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sps.utility.JsonUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Test Json Utility methods */
@RunWith(JUnit4.class)
public class JsonUtilityTest {

  private static class CustomClass {
    String topSecret;

    CustomClass() {
      this.topSecret = "shhh";
    }

    public String revealSecret() {
      return topSecret;
    }
  }

  private static final String STRING_OBJECT = "HELLO WORLD";
  private static final List<Integer> LIST_OBJECT = Arrays.asList(1, 3, 5, 9, 7);
  private static final Map<String, Integer> MAP_OBJECT =
      ImmutableMap.of(
          "One", Integer.valueOf(1), "Two", Integer.valueOf(2), "Three", Integer.valueOf(3));
  private static final CustomClass CUSTOM_OBJECT = new CustomClass();

  private static HttpServletResponse response;
  private static StringWriter stringWriter;
  private static PrintWriter printWriter;
  private final Gson gson = new Gson();

  @Before
  public void setUp() throws IOException {
    // Mock servlet response writer
    response = Mockito.mock(HttpServletResponse.class);
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void sendStringToResponse() throws IOException {
    // Sends an object of type String to response as json.
    // Response should have the same contents as String object.
    JsonUtility.sendJson(response, STRING_OBJECT);
    printWriter.flush();

    String actual = gson.fromJson(stringWriter.toString(), String.class);

    Assert.assertEquals(STRING_OBJECT, actual);
  }

  @Test
  public void sendListToResponse() throws IOException {
    // Sends an object of type List to response as json.
    // Response should have the same contents as List object.
    JsonUtility.sendJson(response, LIST_OBJECT);
    printWriter.flush();

    String actualString = stringWriter.toString();
    Type type = new TypeToken<List<Integer>>() {}.getType();
    List<Integer> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(LIST_OBJECT, actual);
  }

  @Test
  public void sendMapToResponse() throws IOException {
    // Sends an object of type Map to response as json.
    // Response should have the same contents as Map object.
    JsonUtility.sendJson(response, MAP_OBJECT);
    printWriter.flush();

    String actualString = stringWriter.toString();
    Type type = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> actual = gson.fromJson(actualString, type);

    Assert.assertEquals(MAP_OBJECT.get("One"), actual.get("One"));
    Assert.assertEquals(MAP_OBJECT.get("Two"), actual.get("Two"));
    Assert.assertEquals(MAP_OBJECT.get("Three"), actual.get("Three"));
  }

  @Test
  public void sendNullToResponse() throws IOException {
    // Sends null to response as json.
    // Response should contain only a newline character as a result of println.
    JsonUtility.sendJson(response, null);
    printWriter.flush();

    Assert.assertEquals("\n", stringWriter.toString());
  }

  @Test
  public void sendCustomClassToResponse() throws IOException {
    // Sends an object of type CustomClass to response as json.
    // Response should have the same contents as CustomClass object.
    JsonUtility.sendJson(response, CUSTOM_OBJECT);
    printWriter.flush();

    String actualString = stringWriter.toString();
    CustomClass actual = gson.fromJson(actualString, CustomClass.class);

    Assert.assertEquals(CUSTOM_OBJECT.revealSecret(), actual.revealSecret());
  }
}

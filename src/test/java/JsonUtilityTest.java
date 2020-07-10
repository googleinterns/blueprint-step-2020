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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.utility.JsonUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JsonUtilityTest {

  private final String STRING_OBJECT = "HELLO WORLD";
  private final List<Integer> LIST_OBJECT = Arrays.asList(1, 3, 5, 9, 7);
  private final Map<String, Integer> MAP_OBJECT =
      new HashMap<String, Integer>() {
        {
          put("a", 1);
          put("c", 3);
        }
      };

  static HttpServletResponse response = mock(HttpServletResponse.class);
  static StringWriter stringWriter = new StringWriter();
  static PrintWriter printWriter = new PrintWriter(stringWriter);
  private final Gson gson = new Gson();

  @BeforeClass
  public static void init() throws IOException {
    // Dependency injection before tests
    when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void clear() {
    // Dump contents after each test
    stringWriter.getBuffer().setLength(0);
  }

  @Test
  public void sendStringToResponse() throws IOException {
    // String should be sent successfully and in the expected format
    JsonUtility.sendJson(response, STRING_OBJECT);

    String expected = gson.toJson(STRING_OBJECT);
    String actual = stringWriter.toString();

    // "\n" because println is used in JsonUtility.json
    Assert.assertEquals(expected + "\n", actual);
  }

  @Test
  public void sendListToResponse() throws IOException {
    // List should be sent successfully and in the expected format
    JsonUtility.sendJson(response, LIST_OBJECT);

    String expected = gson.toJson(LIST_OBJECT);
    String actual = stringWriter.toString();

    // "\n" because println is used in JsonUtility.json
    Assert.assertEquals(expected + "\n", actual);
  }

  @Test
  public void sendMapToResponse() throws IOException {
    // Map should be sent successfully and in the expected format
    JsonUtility.sendJson(response, MAP_OBJECT);

    String expected = gson.toJson(MAP_OBJECT);
    String actual = stringWriter.toString();

    // "\n" because println is used in JsonUtility.json
    Assert.assertEquals(expected + "\n", actual);
  }
}

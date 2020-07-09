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

// A series of utility functions that will be helpful throughout the front-end

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.sps.utility.JsonUtility;

import org.junit.Assert;
import org.junit.Test;

@RunWith(JUnit4.class)
public class JsonUtilityTest {

  @Test
  public void sendStringToResponse() throws IOException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    String STRING_OBJECT = "HELLO WORLD";
    when(response.getWriter()).thenReturn(printWriter);

    JsonUtility.sendJson(response, STRING_OBJECT);

    Gson gson = new Gson();
    String expected = gson.toJson(STRING_OBJECT);
    String actual = stringWriter.toString();

    Assert.assertEquals(expected + "\n", actual);
  }

}

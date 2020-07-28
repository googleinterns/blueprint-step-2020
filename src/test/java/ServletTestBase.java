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

import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

/**
 * Base test class for testing servlets. Does not contain authentication handling (use
 * AuthenticatedServletTestBase for servlets that require user authentication
 */
public abstract class ServletTestBase {
  protected HttpServletRequest request;
  protected HttpServletResponse response;
  protected StringWriter stringWriter;

  @Before
  public void setUp() throws Exception {
    stringWriter = new StringWriter();
    request = Mockito.mock(HttpServletRequest.class);
    response =
        Mockito.mock(
            HttpServletResponse.class,
            AdditionalAnswers.delegatesTo(new HttpServletResponseFake(stringWriter)));
  }
}

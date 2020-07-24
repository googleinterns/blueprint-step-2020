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

import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.servlets.ClientIDServlet;
import java.io.File;
import java.io.FileWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Client ID Servlet to ensure response contains a defined OAuth 2.0 client ID */
@RunWith(JUnit4.class)
public final class ClientIDServletTest extends ServletTestBase {

  private static final ClientIDServlet servlet = new ClientIDServlet();
  private static final File file = new File("src/main/resources/KEYS.json");

  @Before
  public void setUp() throws Exception {
    super.setUp();
    // Creates a new file in src/main/resources and writes clientId to the file.
    file.getParentFile().mkdirs();
    file.createNewFile();
    FileWriter writer = new FileWriter(file);
    writer.write("{\"clientId\" : \"sampleValue\"}");
    writer.close();
  }

  @After
  public void tearDown() {
    // Removes the file created in src/main/resources from calling setUp.
    file.delete();
  }

  @Test
  public void responseContainsClientId() throws Exception {
    servlet.doGet(request, response);
    Assert.assertTrue(stringWriter.toString().contains(AuthenticationVerifier.getClientId()));
  }
}

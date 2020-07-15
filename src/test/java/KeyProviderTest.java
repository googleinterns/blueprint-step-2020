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

import com.google.sps.utility.KeyProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Key Provider functions */
@RunWith(JUnit4.class)
public class KeyProviderTest {

  private static final String SAMPLE_KEY = "sampleKey";
  private static final String CAPITALISED_SAMPLE_KEY = "SAMPLEKEY";
  private static final String INVALID_KEY = "invalidKey";
  private static final String SAMPLE_VALUE = "sampleValue";
  private static final String KEYS_JSON = "{\"sampleKey\" : \"sampleValue\"}";
  private static final File file = new File("src/main/resources/TEST_KEYS.json");

  @Before
  public void init() {
    // Creates a new file in src/main/resources and writes json content to the file.
    try {
      file.mkdirs();
      file.createNewFile();
      FileWriter writer = new FileWriter("src/main/resources/TEST_KEYS.json");
      writer.write(KEYS_JSON);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @After
  public void tear() {
    // Removes the file created in src/main/resources from calling init()
    file.delete();
  }

  @Test
  public void getSampleKeyValue() throws IOException {
    // Gets the value of sampleKey which is in src/main/resources/TEST_KEYS.json.
    // invalidKey is expected.
    String actual = (new KeyProvider(file)).getKey(SAMPLE_KEY);
    Assert.assertEquals(SAMPLE_VALUE, actual);
  }

  @Test
  public void getCapitalisedSampleKeyValue() throws IOException {
    // Gets the value of SAMPLEKEY which is not in src/main/resources/TEST_KEYS.json since keys are
    // case sensitive.
    // null is expected.
    String actual = (new KeyProvider(file)).getKey(CAPITALISED_SAMPLE_KEY);
    Assert.assertNull(actual);
  }

  @Test
  public void getInvalidKeyValue() throws IOException {
    // Gets the value of an invalid key which is not in src/main/resources/TEST_KEYS.json.
    // null is expected.
    String actual = (new KeyProvider(file)).getKey(INVALID_KEY);
    Assert.assertNull(actual);
  }
}

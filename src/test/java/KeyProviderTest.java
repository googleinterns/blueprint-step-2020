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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Key Provider functions */
@RunWith(JUnit4.class)
public class KeyProviderTest {

  private static final File file = new File("src/main/resources/TEST_KEYS.json");

  @BeforeClass
  public static void setUp() throws Exception {
    // Creates a new file in src/main/resources and writes json content to the file.
    file.getParentFile().mkdirs();
    file.createNewFile();
    FileWriter writer = new FileWriter(file);
    writer.write("{\"sampleKey\" : \"sampleValue\"}");
    writer.close();
  }

  @AfterClass
  public static void tearDown() {
    // Removes the file created in src/main/resources from calling setUp.
    file.delete();
  }

  @Test
  public void getSampleKeyValue() throws Exception {
    // Gets the value of sampleKey which is in src/main/resources/TEST_KEYS.json.
    String actual = (new KeyProvider(file)).getKey("sampleKey");
    Assert.assertEquals("sampleValue", actual);
  }

  @Test
  public void getCapitalisedSampleKeyValue() throws Exception {
    // Gets the value of SAMPLEKEY which is not in src/main/resources/TEST_KEYS.json since keys are
    // case sensitive.
    String actual = (new KeyProvider(file)).getKey("SAMPLEKEY");
    Assert.assertNull(actual);
  }

  @Test
  public void getInvalidKeyValue() throws Exception {
    // Gets the value of an invalid key which is not in src/main/resources/TEST_KEYS.json.
    String actual = (new KeyProvider(file)).getKey("invalidKey");
    Assert.assertNull(actual);
  }
}

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test Key Provider functions. File I/O logic in KeyProvider() constructor with no parameters is
 * not tested. App would fail completely and a FileNotFoundException is thrown if it is not working.
 */
@RunWith(JUnit4.class)
public class KeyProviderTest {
  private KeyProvider keyProvider;

  @Before
  public void setUp() throws Exception {
    keyProvider = new KeyProvider("{\"sampleKey\" : \"sampleValue\"}");
  }

  @Test
  public void getSampleKeyValue() throws Exception {
    // Gets the value of sampleKey which is in the json.
    String actual = keyProvider.getKey("sampleKey");
    Assert.assertEquals("sampleValue", actual);
  }

  @Test
  public void getCapitalisedSampleKeyValue() throws Exception {
    // Gets the value of SAMPLEKEY which is not in the json since keys are case sensitive.
    String actual = keyProvider.getKey("SAMPLEKEY");
    Assert.assertNull(actual);
  }

  @Test
  public void getInvalidKeyValue() throws Exception {
    // Gets the value of an invalid key which is not in the json.
    String actual = keyProvider.getKey("invalidKey");
    Assert.assertNull(actual);
  }
}

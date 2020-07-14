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
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test Key Provider functions */
@RunWith(JUnit4.class)
public class KeyProviderTest {

  private static final String SAMPLE_KEY = "sampleKey";
  private static final String SAMPLE_VALUE = "sampleValue";

  @Test
  public void getSampleKeyValue() throws IOException {
    String expected = SAMPLE_VALUE;
    String actual = KeyProvider.getKey(SAMPLE_KEY);
    Assert.assertEquals(expected, actual);
  }
}

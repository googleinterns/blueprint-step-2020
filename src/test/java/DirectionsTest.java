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

import com.google.maps.DirectionsApplication;
import com.google.maps.DirectionsConsumer;
import com.google.maps.DirectionsService;
import com.google.maps.DirectionsServiceInjector;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DirectionsTest {

  private DirectionsServiceInjector injector;
  private final String ORIGIN = "University of Waterloo";
  private final String DESTINATION = "Wilfrid Laurier University";
  private final List<String> ONE_WAYPOINT = Arrays.asList("University of Waterloo Place");
  private final String API_KEY = "SAMPLE_API_KEY";

  @Before
  public void init() {
    // Mock the injector with an anonymous class
    injector =
        new DirectionsServiceInjector() {
          @Override
          public DirectionsConsumer getConsumer() {
            // Mock the Directions service
            return new DirectionsApplication(
                new DirectionsService() {
                  @Override
                  public String getDirections(String uri) {
                    System.out.println("Mock Directions Service Implementation");
                    return null;
                  }
                });
          }
        };
  }

  @After
  public void tear() {
    injector = null;
  }

  @Test
  public void getDirections() {
    // Destination and origin are not the same, only one waypoint.
    // null is expected since Directions service is mocked.
    DirectionsConsumer consumer = injector.getConsumer();
    String result = consumer.processDirections(DESTINATION, ORIGIN, ONE_WAYPOINT, API_KEY);
    Assert.assertNull(result);
  }
}

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

import com.google.api.services.tasks.model.Task;
import com.google.common.collect.ImmutableList;
import com.google.sps.model.DirectionsClientFactory;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.servlets.GoServlet;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class GoServletTest extends AuthenticatedServletTestBase {

  private DirectionsClientFactory directionsClientFactory;
  private TasksClientFactory tasksClientFactory;
  private GoServlet servlet;

  private static final String PREFIX = "Location";

  private static final Task TASK_WITH_NO_NOTES = new Task();
  private static final Task TASK_WITH_NO_LOCATION = new Task().setNotes("sample notes");
  private static final Task TASK_WITH_ONE_LOCATION =
      new Task().setNotes("sample notes [Location: Google Kitchener] more sample notes");
  private static final Task TASK_WITH_TWO_LOCATIONS =
      new Task().setNotes("sample notes [Location: Google Kitchener] [Location: Google Montreal]");
  private static final Task TASK_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY =
      new Task().setNotes("(Location: Google Kitchener)");
  private static final Task TASK_WITH_EMPTY_LOCATION = new Task().setNotes("[Location: ]");

  private static final List<Task> NO_TASKS = ImmutableList.of();
  private static final List<Task> TASKS_WITH_NO_NOTES = ImmutableList.of(TASK_WITH_NO_NOTES);
  private static final List<Task> TASKS_WITH_NO_LOCATION = ImmutableList.of(TASK_WITH_NO_LOCATION);
  private static final List<Task> TASKS_WITH_ONE_LOCATION =
      ImmutableList.of(TASK_WITH_ONE_LOCATION);
  private static final List<Task> TASKS_WITH_TWO_LOCATIONS =
      ImmutableList.of(TASK_WITH_TWO_LOCATIONS);
  private static final List<Task> TASKS_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY =
      ImmutableList.of(TASK_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY);
  private static final List<Task> TASKS_WITH_EMPTY_LOCATION =
      ImmutableList.of(TASK_WITH_EMPTY_LOCATION);
  private static final List<Task> ALL_TASKS =
      ImmutableList.of(
          TASK_WITH_NO_NOTES,
          TASK_WITH_NO_LOCATION,
          TASK_WITH_ONE_LOCATION,
          TASK_WITH_TWO_LOCATIONS,
          TASK_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY,
          TASK_WITH_EMPTY_LOCATION);

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    directionsClientFactory = Mockito.mock(DirectionsClientFactory.class);
    tasksClientFactory = Mockito.mock(TasksClientFactory.class);
    servlet =
        new GoServlet(
            directionsClientFactory,
            tasksClientFactory,
            "fakeApiKey",
            "fakeOrigin",
            "fakeDestination",
            ImmutableList.of("fakeWaypoints"));
  }

  @Test
  public void testOne() {
    Assert.assertTrue(true);
  }
}

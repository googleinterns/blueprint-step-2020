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
import com.google.sps.utility.LocationsUtility;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LocationsUtilityTest {

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

  @Test
  public void getLocationNoTasks() {
    // Obtain locations in the the task notes of no tasks.
    Assert.assertEquals(ImmutableList.of(), LocationsUtility.getLocations(PREFIX, NO_TASKS));
  }

  @Test
  public void getLocationNoNotes() {
    // Obtain location in the task notes of one task with no notes defined.
    Assert.assertEquals(
        ImmutableList.of(), LocationsUtility.getLocations(PREFIX, TASKS_WITH_NO_NOTES));
  }

  @Test
  public void getNoLocation() {
    // Obtain location in the task notes of one task with no location.
    Assert.assertEquals(
        ImmutableList.of(), LocationsUtility.getLocations(PREFIX, TASKS_WITH_NO_LOCATION));
  }

  @Test
  public void getOneLocation() {
    // Obtain location in the task notes of one task with one location.
    Assert.assertEquals(
        ImmutableList.of("Google Kitchener"),
        LocationsUtility.getLocations(PREFIX, TASKS_WITH_ONE_LOCATION));
  }

  @Test
  public void getTwoLocations() {
    // Obtain location in the task notes of one task with two locations. Second location, Google
    // Montreal, is ignored.
    Assert.assertEquals(
        ImmutableList.of("Google Kitchener"),
        LocationsUtility.getLocations(PREFIX, TASKS_WITH_TWO_LOCATIONS));
  }

  @Test
  public void getIncorrectlyEnclosedLocation() {
    // Obtain location in the task notes of one task with one location enclosed with () instead of
    // [].
    Assert.assertEquals(
        ImmutableList.of(),
        LocationsUtility.getLocations(PREFIX, TASKS_WITH_ONE_LOCATION_ENCLOSED_INCORRECTLY));
  }

  @Test
  public void getEmptyLocation() {
    // Obtain location in the task notes of one task with one location with [Location: ] tag but
    // nothing inside of it.
    Assert.assertEquals(
        ImmutableList.of(""), LocationsUtility.getLocations(PREFIX, TASKS_WITH_EMPTY_LOCATION));
  }

  @Test
  public void getLocationAllTasks() {
    // Obtain location in the task notes of five tasks each with either one, empty or no location as
    // noted in the individual tests above.
    Assert.assertEquals(
        ImmutableList.of("Google Kitchener", "Google Kitchener", ""),
        LocationsUtility.getLocations(PREFIX, ALL_TASKS));
  }
  
  @Test
  public void testGetPermutations() {
    List<List<String>> searchResults = ImmutableList.of(ImmutableList.of("1", "2"), ImmutableList.of("3", "4", "5"));
    List<List<String>> result = new ArrayList<List<String>>();

    LocationsUtility.generateCombinations(searchResults, result, 0, new ArrayList<String>());
    System.out.println(result);
    Assert.assertNotNull(result);
  }
}

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
import com.google.api.services.tasks.model.TaskList;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import com.google.sps.model.TasksClient;
import com.google.sps.utility.TasksUtility;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Test Tasks Utility functions */
@RunWith(JUnit4.class)
public final class TasksUtilityTest {

  private TasksClient tasksClient;

  private static final String TASK_ID_ONE = "task one";
  private static final String TASK_ID_TWO = "task two";
  private static final String TASK_LIST_ID_ONE = "task list one";
  private static final String TASK_LIST_ID_TWO = "task list two";

  private static final Task TASK_ONE = new Task().setId(TASK_ID_ONE);
  private static final Task TASK_TWO = new Task().setId(TASK_ID_TWO);

  private static final TaskList TASK_LIST_ONE = new TaskList().setId(TASK_LIST_ID_ONE);
  private static final TaskList TASK_LIST_TWO = new TaskList().setId(TASK_LIST_ID_TWO);

  private static final List<TaskList> NO_TASK_LISTS = ImmutableList.of();
  private static final List<TaskList> SOME_TASK_LISTS =
      ImmutableList.of(TASK_LIST_ONE, TASK_LIST_TWO);

  private static final List<Task> NO_TASKS = ImmutableList.of();
  private static final List<Task> ONE_TASK = ImmutableList.of(TASK_ONE);
  private static final List<Task> SOME_TASKS = ImmutableList.of(TASK_ONE, TASK_TWO);

  @Before
  public void setUp() {
    tasksClient = Mockito.mock(TasksClient.class);
  }

  @Test
  public void getAllTasksFromNoTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(NO_TASK_LISTS);
    List<Task> actual = TasksUtility.getAllTasksFromAllTaskLists(tasksClient);
    Assert.assertTrue(actual.isEmpty());
  }

  @Test
  public void getNoTasksFromSomeTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASK_LISTS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(NO_TASKS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_TWO)).thenReturn(NO_TASKS);
    List<Task> actual = TasksUtility.getAllTasksFromAllTaskLists(tasksClient);
    Assert.assertTrue(NO_TASKS.containsAll(actual));
    Assert.assertTrue(actual.containsAll(NO_TASKS));
  }

  @Test
  public void getSomeTasksFromSomeTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASK_LISTS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ONE_TASK);
    Mockito.when(tasksClient.listTasks(TASK_LIST_TWO)).thenReturn(SOME_TASKS);
    List<Task> actual = TasksUtility.getAllTasksFromAllTaskLists(tasksClient);
    List<Task> expected = ImmutableList.of(TASK_ONE, TASK_ONE, TASK_TWO);
    Assert.assertTrue(expected.containsAll(actual));
    Assert.assertTrue(actual.containsAll(expected));
  }

  @Test
  public void getAllTasksFromNoSelectedTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASK_LISTS);
    List<Task> actual =
        TasksUtility.getAllTasksFromSpecificTaskLists(tasksClient, ImmutableSet.of());
    Assert.assertTrue(NO_TASKS.containsAll(actual));
    Assert.assertTrue(actual.containsAll(NO_TASKS));
  }

  @Test
  public void getAllTasksFromSelectedTaskList() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASK_LISTS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(SOME_TASKS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_TWO)).thenReturn(SOME_TASKS);
    List<Task> actual =
        TasksUtility.getAllTasksFromSpecificTaskLists(
            tasksClient, ImmutableSet.of(TASK_LIST_ID_ONE));
    Assert.assertEquals(SOME_TASKS, actual);
    Assert.assertTrue(SOME_TASKS.containsAll(actual));
    Assert.assertTrue(actual.containsAll(SOME_TASKS));
  }
}

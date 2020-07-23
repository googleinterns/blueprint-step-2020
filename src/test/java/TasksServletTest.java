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
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.common.collect.ImmutableList;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.model.TasksResponse;
import com.google.sps.servlets.TasksServlet;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

/**
 * Test that the Tasks Servlet responds to the client with correct TasksResponse. Assumes
 * AuthenticatedHttpServlet is functioning properly (those tests will fail otherwise).
 */
@RunWith(JUnit4.class)
public final class TasksServletTest extends AuthenticatedServletTestBase {
  private TasksClientFactory tasksClientFactory;
  private TasksClient tasksClient;
  private TasksServlet servlet;

  private static final Gson gson = new Gson();

  private static final String TASK_TITLE_ONE = "task one";
  private static final String TASK_TITLE_TWO = "task two";
  private static final String TASK_TITLE_THREE = "task three";
  private static final String TASK_TITLE_FOUR = "task four";
  private static final String TASK_TITLE_FIVE = "task five";
  private static final String TASKLIST_TITLE_ONE = "task list one";

  private static final TaskList TASKLIST_ONE =
      new TaskList().setTitle(TASKLIST_TITLE_ONE).setId("taskListOne");

  private static final String TASKLIST_ID_ONE = "taskListOne";
  private static final String TASKLIST_ID_TWO = "taskListTwo";

  private static final List<TaskList> NO_TASKLISTS = ImmutableList.of();
  private static final List<String> NO_TASKLISTS_TITLES = ImmutableList.of();
  private static final List<TaskList> ONE_TASKLIST = ImmutableList.of(TASKLIST_ONE);
  private static final List<String> ONE_TASKLIST_TITLES = ImmutableList.of(TASKLIST_TITLE_ONE);

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneId.systemDefault());

  private static final String YESTERDAY = FORMATTER.format(Instant.now().minus(1, ChronoUnit.DAYS));
  private static final String TODAY = FORMATTER.format(Instant.now());
  private static final String TOMORROW = FORMATTER.format(Instant.now().plus(1, ChronoUnit.DAYS));

  private static final Task TASK_DUE_YESTERDAY =
      new Task().setTitle(TASK_TITLE_TWO).setDue(YESTERDAY);
  private static final Task TASK_DUE_TODAY = new Task().setTitle(TASK_TITLE_ONE).setDue(TODAY);
  private static final Task TASK_DUE_TOMORROW =
      new Task().setTitle(TASK_TITLE_THREE).setDue(TOMORROW);
  private static final Task TASK_COMPLETED_YESTERDAY =
      new Task().setTitle(TASK_TITLE_FOUR).setHidden(true).setUpdated(YESTERDAY);
  private static final Task TASK_COMPLETED_TODAY =
      new Task().setTitle(TASK_TITLE_FIVE).setHidden(true).setUpdated(TODAY);

  private static final List<Task> NO_TASKS = ImmutableList.of();
  private static final List<Task> TASKS_DUE_YESTERDAY = ImmutableList.of(TASK_DUE_YESTERDAY);
  private static final List<Task> TASKS_DUE_TODAY = ImmutableList.of(TASK_DUE_TODAY);
  private static final List<Task> TASKS_DUE_TOMORROW = ImmutableList.of(TASK_DUE_TOMORROW);
  private static final List<Task> TASKS_COMPLETED_YESTERDAY =
      ImmutableList.of(TASK_COMPLETED_YESTERDAY);
  private static final List<Task> TASKS_COMPLETED_TODAY = ImmutableList.of(TASK_COMPLETED_TODAY);
  private static final List<Task> ALL_TASKS =
      ImmutableList.of(
          TASK_DUE_YESTERDAY,
          TASK_DUE_TODAY,
          TASK_DUE_TOMORROW,
          TASK_COMPLETED_YESTERDAY,
          TASK_COMPLETED_TODAY);

  private static final String SAMPLE_NOTES = "sample notes";
  private static final String DUE_DATE = "2020-07-20T00:00:00.000Z";
  private static final Task validTask =
      new Task().setTitle(TASK_TITLE_ONE).setNotes(SAMPLE_NOTES).setDue(DUE_DATE);
  private static final String VALID_TASK_JSON = gson.toJson(validTask);

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    tasksClientFactory = Mockito.mock(TasksClientFactory.class);
    tasksClient = Mockito.mock(TasksClient.class);
    servlet = new TasksServlet(authenticationVerifier, tasksClientFactory);

    Mockito.when(tasksClientFactory.getTasksClient(Mockito.any())).thenReturn(tasksClient);
  }

  @Test
  public void noTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(NO_TASKLISTS);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(NO_TASKLISTS_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(0, tasksResponse.getTasksToComplete());
    Assert.assertEquals(0, tasksResponse.getTasksDueToday());
    Assert.assertEquals(0, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(0, tasksResponse.getTasksOverdue());
  }

  @Test
  public void noTasks() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASKLIST);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(NO_TASKS);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ONE_TASKLIST_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(0, tasksResponse.getTasksToComplete());
    Assert.assertEquals(0, tasksResponse.getTasksDueToday());
    Assert.assertEquals(0, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(0, tasksResponse.getTasksOverdue());
  }

  @Test
  public void taskOverdue() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASKLIST);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(TASKS_DUE_YESTERDAY);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ONE_TASKLIST_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(1, tasksResponse.getTasksToComplete());
    Assert.assertEquals(0, tasksResponse.getTasksDueToday());
    Assert.assertEquals(0, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(1, tasksResponse.getTasksOverdue());
  }

  @Test
  public void taskDueToday() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASKLIST);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(TASKS_DUE_TODAY);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ONE_TASKLIST_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(1, tasksResponse.getTasksToComplete());
    Assert.assertEquals(1, tasksResponse.getTasksDueToday());
    Assert.assertEquals(0, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(0, tasksResponse.getTasksOverdue());
  }

  @Test
  public void taskToComplete() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASKLIST);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(TASKS_DUE_TOMORROW);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ONE_TASKLIST_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(1, tasksResponse.getTasksToComplete());
    Assert.assertEquals(0, tasksResponse.getTasksDueToday());
    Assert.assertEquals(0, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(0, tasksResponse.getTasksOverdue());
  }

  @Test
  public void taskCompletedYesterday() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASKLIST);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(TASKS_COMPLETED_YESTERDAY);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ONE_TASKLIST_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(0, tasksResponse.getTasksToComplete());
    Assert.assertEquals(0, tasksResponse.getTasksDueToday());
    Assert.assertEquals(0, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(0, tasksResponse.getTasksOverdue());
  }

  @Test
  public void taskCompletedToday() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASKLIST);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(TASKS_COMPLETED_TODAY);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ONE_TASKLIST_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(0, tasksResponse.getTasksToComplete());
    Assert.assertEquals(0, tasksResponse.getTasksDueToday());
    Assert.assertEquals(1, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(0, tasksResponse.getTasksOverdue());
  }

  @Test
  public void allTasks() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASKLIST);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(ALL_TASKS);
    servlet.doGet(request, response);
    TasksResponse tasksResponse = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ONE_TASKLIST_TITLES, tasksResponse.getTaskListNames());
    Assert.assertEquals(3, tasksResponse.getTasksToComplete());
    Assert.assertEquals(1, tasksResponse.getTasksDueToday());
    Assert.assertEquals(1, tasksResponse.getTasksCompletedToday());
    Assert.assertEquals(1, tasksResponse.getTasksOverdue());
  }

  @Test
  public void postTaskNullTaskListId() throws Exception {
    servlet.doPost(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postTaskEmptyTaskListId() throws Exception {
    Mockito.when(request.getParameter("taskListId")).thenReturn("");
    servlet.doPost(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postValidTask() throws Exception {
    Mockito.when(request.getParameter("taskListId")).thenReturn(TASKLIST_ID_ONE);
    Mockito.when(tasksClient.postTask(TASKLIST_ID_ONE, validTask)).thenReturn(validTask);

    StringReader reader = new StringReader(VALID_TASK_JSON);
    BufferedReader bufferedReader = new BufferedReader(reader);
    Mockito.when(request.getReader()).thenReturn(bufferedReader);

    servlet.doPost(request, response);

    Task postedTask = gson.fromJson(stringWriter.toString(), Task.class);

    Assert.assertEquals(validTask.getTitle(), postedTask.getTitle());
    Assert.assertEquals(validTask.getNotes(), postedTask.getNotes());
    Assert.assertEquals(validTask.getDue(), postedTask.getDue());
  }
}

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
import com.google.common.collect.ImmutableMap;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailResponse;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.model.TasksResponse;
import com.google.sps.servlets.TasksServlet;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
  private AuthenticationVerifier authenticationVerifier;
  private TasksClientFactory tasksClientFactory;
  private TasksClient tasksClient;
  private TasksServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;

  private static final Gson gson = new Gson();

  private static final String TASK_TITLE_ONE = "task one";
  private static final String TASK_TITLE_TWO = "task two";
  private static final String TASK_TITLE_THREE = "task three";
  private static final String TASK_TITLE_FOUR = "task four";
  private static final String TASK_TITLE_FIVE = "task five";
  private static final String TASK_LIST_TITLE_ONE = "task list title one";
  private static final String TASK_LIST_TITLE_TWO = "task list title two";
  private static final String TASK_LIST_ID_ONE = "task list id one";
  private static final String TASK_LIST_ID_TWO = "task list id two";

  private static final TaskList TASK_LIST_ONE =
      new TaskList().setTitle(TASK_LIST_TITLE_ONE).setId(TASK_LIST_ID_ONE);
  private static final TaskList TASK_LIST_TWO =
      new TaskList().setTitle(TASK_LIST_TITLE_TWO).setId(TASK_LIST_ID_TWO);

  private static final List<TaskList> NO_TASK_LISTS = ImmutableList.of();
  private static final List<TaskList> ONE_TASK_LIST = ImmutableList.of(TASK_LIST_ONE);
  private static final List<TaskList> TWO_TASK_LISTS =
      ImmutableList.of(TASK_LIST_ONE, TASK_LIST_TWO);
  private static final Map<String, String> NO_TASK_LIST_IDS_TO_TITLES = ImmutableMap.of();
  private static final Map<String, String> ONE_TASK_LIST_IDS_TO_TITLES =
      ImmutableMap.of(TASK_LIST_ID_ONE, TASK_LIST_TITLE_ONE);
  private static final Map<String, String> TWO_TASK_LIST_IDS_TO_TITLES =
      ImmutableMap.of(TASK_LIST_ID_ONE, TASK_LIST_TITLE_ONE, TASK_LIST_ID_TWO, TASK_LIST_TITLE_TWO);

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

  private static final String EMPTY_JSON = "{}";
  private static final String INVALID_TASK_JSON = gson.toJson(new GmailResponse(0, 0, 0, ""));

  private static final TasksResponse NO_TASK_LISTS_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(NO_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(0)
          .tasksDueTodayCount(0)
          .tasksCompletedTodayCount(0)
          .tasksOverdueCount(0)
          .build();
  private static final TasksResponse NO_TASKS_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(ONE_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(0)
          .tasksDueTodayCount(0)
          .tasksCompletedTodayCount(0)
          .tasksOverdueCount(0)
          .build();
  private static final TasksResponse TASKS_DUE_YESTERDAY_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(ONE_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(1)
          .tasksDueTodayCount(0)
          .tasksCompletedTodayCount(0)
          .tasksOverdueCount(1)
          .build();
  private static final TasksResponse TASKS_DUE_TODAY_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(ONE_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(1)
          .tasksDueTodayCount(1)
          .tasksCompletedTodayCount(0)
          .tasksOverdueCount(0)
          .build();
  private static final TasksResponse TASKS_DUE_TOMORROW_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(ONE_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(1)
          .tasksDueTodayCount(0)
          .tasksCompletedTodayCount(0)
          .tasksOverdueCount(0)
          .build();
  private static final TasksResponse TASKS_COMPLETED_YESTERDAY_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(ONE_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(0)
          .tasksDueTodayCount(0)
          .tasksCompletedTodayCount(0)
          .tasksOverdueCount(0)
          .build();
  private static final TasksResponse TASKS_COMPLETED_TODAY_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(ONE_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(0)
          .tasksDueTodayCount(0)
          .tasksCompletedTodayCount(1)
          .tasksOverdueCount(0)
          .build();
  private static final TasksResponse ALL_TASKS_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(ONE_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(3)
          .tasksDueTodayCount(1)
          .tasksCompletedTodayCount(1)
          .tasksOverdueCount(1)
          .build();
  private static final TasksResponse MULTIPLE_TASK_LISTS_RESPONSE =
      TasksResponse.builder()
          .taskListIdsToTitles(TWO_TASK_LIST_IDS_TO_TITLES)
          .tasksToCompleteCount(6)
          .tasksDueTodayCount(2)
          .tasksCompletedTodayCount(2)
          .tasksOverdueCount(2)
          .build();

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    authenticationVerifier = Mockito.mock(AuthenticationVerifier.class);
    tasksClientFactory = Mockito.mock(TasksClientFactory.class);
    tasksClient = Mockito.mock(TasksClient.class);
    servlet = new TasksServlet(authenticationVerifier, tasksClientFactory);

    Mockito.when(tasksClientFactory.getTasksClient(Mockito.any())).thenReturn(tasksClient);
    // Authentication will always pass
    Mockito.when(authenticationVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);

    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();

    request = Mockito.mock(HttpServletRequest.class);
    response =
        Mockito.mock(
            HttpServletResponse.class,
            AdditionalAnswers.delegatesTo(new HttpServletResponseFake(stringWriter)));

    Mockito.when(request.getCookies()).thenReturn(validCookies);
  }

  @Test
  public void noTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(NO_TASK_LISTS);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(NO_TASK_LISTS_RESPONSE, actual);
  }

  @Test
  public void noTasks() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(NO_TASKS);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(NO_TASKS_RESPONSE, actual);
  }

  @Test
  public void taskOverdue() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(TASKS_DUE_YESTERDAY);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(TASKS_DUE_YESTERDAY_RESPONSE, actual);
  }

  @Test
  public void taskDueToday() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(TASKS_DUE_TODAY);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(TASKS_DUE_TODAY_RESPONSE, actual);
  }

  @Test
  public void taskToComplete() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(TASKS_DUE_TOMORROW);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(TASKS_DUE_TOMORROW_RESPONSE, actual);
  }

  @Test
  public void taskCompletedYesterday() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(TASKS_COMPLETED_YESTERDAY);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(TASKS_COMPLETED_YESTERDAY_RESPONSE, actual);
  }

  @Test
  public void taskCompletedToday() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(TASKS_COMPLETED_TODAY);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(TASKS_COMPLETED_TODAY_RESPONSE, actual);
  }

  @Test
  public void allTasks() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ALL_TASKS);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ALL_TASKS_RESPONSE, actual);
  }

  @Test
  public void multipleTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(TWO_TASK_LISTS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ALL_TASKS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_TWO)).thenReturn(ALL_TASKS);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(MULTIPLE_TASK_LISTS_RESPONSE, actual);
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
  public void postBodyEmpty() throws Exception {
    Mockito.when(request.getParameter("taskListId")).thenReturn(TASK_LIST_ID_ONE);

    StringReader reader = new StringReader("");
    BufferedReader bufferedReader = new BufferedReader(reader);
    Mockito.when(request.getReader()).thenReturn(bufferedReader);
    servlet.doPost(request, response);

    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postEmptyTask() throws Exception {
    Mockito.when(request.getParameter("taskListId")).thenReturn(TASK_LIST_ID_ONE);

    StringReader reader = new StringReader(EMPTY_JSON);
    BufferedReader bufferedReader = new BufferedReader(reader);
    Mockito.when(request.getReader()).thenReturn(bufferedReader);
    servlet.doPost(request, response);

    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postInvalidTask() throws Exception {
    Mockito.when(request.getParameter("taskListId")).thenReturn(TASK_LIST_ID_ONE);

    StringReader reader = new StringReader(INVALID_TASK_JSON);
    BufferedReader bufferedReader = new BufferedReader(reader);
    Mockito.when(request.getReader()).thenReturn(bufferedReader);
    servlet.doPost(request, response);

    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postValidTask() throws Exception {
    Mockito.when(request.getParameter("taskListId")).thenReturn(TASK_LIST_ID_ONE);
    Mockito.when(tasksClient.postTask(TASK_LIST_ID_ONE, validTask)).thenReturn(validTask);

    StringReader reader = new StringReader(VALID_TASK_JSON);
    BufferedReader bufferedReader = new BufferedReader(reader);
    Mockito.when(request.getReader()).thenReturn(bufferedReader);
    servlet.doPost(request, response);

    Task postedTask = gson.fromJson(stringWriter.toString(), Task.class);

    Assert.assertEquals(validTask, postedTask);
  }

  @Test
  public void noTaskListSelected() throws Exception {
    // No task lists selected is equivalent to all task lists selected
    Mockito.when(request.getParameter("taskLists")).thenReturn(null);
    Mockito.when(tasksClient.listTaskLists()).thenReturn(TWO_TASK_LISTS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ALL_TASKS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_TWO)).thenReturn(ALL_TASKS);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(MULTIPLE_TASK_LISTS_RESPONSE, actual);
  }

  @Test
  public void oneTaskListSelected() throws Exception {
    Mockito.when(request.getParameter("taskLists")).thenReturn(TASK_LIST_ID_ONE);
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ALL_TASKS);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(ALL_TASKS_RESPONSE, actual);
  }

  @Test
  public void allTaskListsSelected() throws Exception {
    Mockito.when(request.getParameter("taskLists"))
        .thenReturn(TASK_LIST_ID_ONE + "," + TASK_LIST_ID_TWO);
    Mockito.when(tasksClient.listTaskLists()).thenReturn(TWO_TASK_LISTS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ALL_TASKS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_TWO)).thenReturn(ALL_TASKS);
    servlet.doGet(request, response);
    TasksResponse actual = gson.fromJson(stringWriter.toString(), TasksResponse.class);
    Assert.assertEquals(MULTIPLE_TASK_LISTS_RESPONSE, actual);
  }
}

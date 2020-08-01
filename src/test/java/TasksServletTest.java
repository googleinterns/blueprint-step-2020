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
import com.google.gson.reflect.TypeToken;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.GmailResponse;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.servlets.TasksServlet;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.List;
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
  private static final String TASK_LIST_TITLE_ONE = "task list title one";
  private static final String TASK_LIST_TITLE_TWO = "task list title two";
  private static final String TASK_LIST_ID_ONE = "task list id one";
  private static final String TASK_LIST_ID_TWO = "task list id two";

  private static final Task TASK_ONE = new Task().setTitle(TASK_TITLE_ONE);
  private static final Task TASK_TWO = new Task().setTitle(TASK_TITLE_TWO);

  private static final List<Task> NO_TASKS = ImmutableList.of();
  private static final List<Task> ONE_TASK = ImmutableList.of(TASK_ONE);
  private static final List<Task> TWO_TASKS = ImmutableList.of(TASK_ONE, TASK_TWO);

  private static final TaskList TASK_LIST_ONE =
      new TaskList().setTitle(TASK_LIST_TITLE_ONE).setId(TASK_LIST_ID_ONE);
  private static final TaskList TASK_LIST_TWO =
      new TaskList().setTitle(TASK_LIST_TITLE_TWO).setId(TASK_LIST_ID_TWO);

  private static final List<TaskList> NO_TASK_LISTS = ImmutableList.of();
  private static final List<TaskList> ONE_TASK_LIST = ImmutableList.of(TASK_LIST_ONE);
  private static final List<TaskList> TWO_TASK_LISTS =
      ImmutableList.of(TASK_LIST_ONE, TASK_LIST_TWO);

  private static final String SAMPLE_NOTES = "sample notes";
  private static final String DUE_DATE = "2020-07-20T00:00:00.000Z";
  private static final Task validTask =
      new Task().setTitle(TASK_TITLE_ONE).setNotes(SAMPLE_NOTES).setDue(DUE_DATE);
  private static final String VALID_TASK_JSON = gson.toJson(validTask);

  private static final String EMPTY_JSON = "{}";
  private static final String INVALID_TASK_JSON = gson.toJson(new GmailResponse(0, 0, 0, ""));

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
  public void getNoTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(NO_TASK_LISTS);
    servlet.doGet(request, response);

    Type type = new TypeToken<List<Task>>() {}.getType();
    List<Task> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(NO_TASKS, actual);
  }

  @Test
  public void getNoTasks() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(NO_TASKS);
    servlet.doGet(request, response);

    Type type = new TypeToken<List<Task>>() {}.getType();
    List<Task> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(NO_TASKS, actual);
  }

  @Test
  public void getOneTask() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ONE_TASK);
    servlet.doGet(request, response);

    Type type = new TypeToken<List<Task>>() {}.getType();
    List<Task> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(ONE_TASK, actual);
  }

  @Test
  public void getTwoTasks() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(ONE_TASK_LIST);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(TWO_TASKS);
    servlet.doGet(request, response);

    Type type = new TypeToken<List<Task>>() {}.getType();
    List<Task> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(TWO_TASKS, actual);
  }

  @Test
  public void getTwoTaskLists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(TWO_TASK_LISTS);
    Mockito.when(tasksClient.listTasks(TASK_LIST_ONE)).thenReturn(ONE_TASK);
    Mockito.when(tasksClient.listTasks(TASK_LIST_TWO)).thenReturn(TWO_TASKS);
    servlet.doGet(request, response);

    Type type = new TypeToken<List<Task>>() {}.getType();
    List<Task> actual = gson.fromJson(stringWriter.toString(), type);

    Assert.assertEquals(ImmutableList.of(TASK_ONE, TASK_ONE, TASK_TWO), actual);
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
}

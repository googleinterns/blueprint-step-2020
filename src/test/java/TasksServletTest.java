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
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;
import com.google.common.collect.ImmutableList;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.servlets.TasksServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/**
 * Test Tasks Servlet responds to client with correctly parsed Task names. Assumes
 * AuthenticatedHttpServlet is functioning properly (those tests will fail otherwise).
 */
@RunWith(JUnit4.class)
public final class TasksServletTest {
  private AuthenticationVerifier authenticationVerifier;
  private TasksClientFactory tasksClientFactory;
  private TasksClient tasksClient;
  private TasksServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  private static final Gson gson = new Gson();
  private static final Type LIST_OF_TASKS_TYPE = new TypeToken<List<Task>>() {}.getType();

  private static final boolean AUTHENTICATION_VERIFIED = true;
  private static final String ID_TOKEN_KEY = "idToken";
  private static final String ID_TOKEN_VALUE = "sampleId";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String ACCESS_TOKEN_VALUE = "sampleAccessToken";
  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

  // Tasks must be returned in order of retrieval - JSON includes tasks in desired order
  private static final String TASK_TITLE_ONE = "task one";
  private static final String TASK_TITLE_TWO = "task two";
  private static final String TASK_TITLE_THREE = "task three";
  private static final String TASK_TITLE_FOUR = "task four";

  private static final TaskList TASKLIST_ONE = new TaskList().setId("taskListOne");
  private static final TaskList TASKLIST_TWO = new TaskList().setId("taskListTwo");
  private static final List<TaskList> NO_TASKLISTS = ImmutableList.of();
  private static final List<TaskList> SOME_TASKLISTS = ImmutableList.of(TASKLIST_ONE, TASKLIST_TWO);

  private static final List<Task> NO_TASKS = ImmutableList.of();
  private static final List<Task> TASKS_ONE_TWO =
      ImmutableList.of(new Task().setTitle(TASK_TITLE_ONE), new Task().setTitle(TASK_TITLE_TWO));
  private static final List<Task> TASKS_THREE_FOUR =
      ImmutableList.of(new Task().setTitle(TASK_TITLE_THREE), new Task().setTitle(TASK_TITLE_FOUR));

  @Before
  public void setUp() throws IOException, GeneralSecurityException {
    authenticationVerifier = Mockito.mock(AuthenticationVerifier.class);
    tasksClientFactory = Mockito.mock(TasksClientFactory.class);
    tasksClient = Mockito.mock(TasksClient.class);
    servlet = new TasksServlet(authenticationVerifier, tasksClientFactory);

    Mockito.when(tasksClientFactory.getTasksClient(Mockito.any())).thenReturn(tasksClient);
    // Authentication will always pass
    Mockito.when(authenticationVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);

    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(request.getCookies()).thenReturn(validCookies);

    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    Mockito.when(response.getWriter()).thenReturn(printWriter);
  }

  @Test
  public void noTaskLists() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(NO_TASKLISTS);
    servlet.doGet(request, response);
    printWriter.flush();
    List<Task> tasks = gson.fromJson(stringWriter.toString(), LIST_OF_TASKS_TYPE);
    Assert.assertTrue(tasks.isEmpty());
  }

  @Test
  public void emptyTaskLists() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASKLISTS);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(NO_TASKS);
    Mockito.when(tasksClient.listTasks(TASKLIST_TWO)).thenReturn(NO_TASKS);
    servlet.doGet(request, response);
    printWriter.flush();
    List<Task> tasks = gson.fromJson(stringWriter.toString(), LIST_OF_TASKS_TYPE);
    Assert.assertTrue(tasks.isEmpty());
  }

  @Test
  public void oneEmptyTaskList() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASKLISTS);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(NO_TASKS);
    Mockito.when(tasksClient.listTasks(TASKLIST_TWO)).thenReturn(TASKS_ONE_TWO);
    servlet.doGet(request, response);
    printWriter.flush();
    List<Task> tasks = gson.fromJson(stringWriter.toString(), LIST_OF_TASKS_TYPE);
    tasks.forEach(
        (task) -> {
          Assert.assertTrue(TASKS_ONE_TWO.contains(task) || TASKS_THREE_FOUR.contains(task));
        });
  }

  @Test
  public void completeTaskList() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASKLISTS);
    Mockito.when(tasksClient.listTasks(TASKLIST_ONE)).thenReturn(TASKS_ONE_TWO);
    Mockito.when(tasksClient.listTasks(TASKLIST_TWO)).thenReturn(TASKS_THREE_FOUR);
    servlet.doGet(request, response);
    printWriter.flush();
    List<Task> tasks = gson.fromJson(stringWriter.toString(), LIST_OF_TASKS_TYPE);
    tasks.forEach(
        (task) -> {
          Assert.assertTrue(TASKS_ONE_TWO.contains(task) || TASKS_THREE_FOUR.contains(task));
        });
  }
}

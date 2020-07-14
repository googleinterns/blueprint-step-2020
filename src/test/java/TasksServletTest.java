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
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.servlets.TasksServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/**
 * Test Tasks Servlet responds to client with correctly parsed Task names Assumes
 * AuthenticatedHttpServlet is functioning properly (those tests will fail otherwise).
 */
@RunWith(JUnit4.class)
public final class TasksServletTest {
  private static final AuthenticationVerifier authenticationVerifier =
      Mockito.mock(AuthenticationVerifier.class);
  private static final TasksClientFactory tasksClientFactory =
      Mockito.mock(TasksClientFactory.class);
  private static final TasksClient tasksClient = Mockito.mock(TasksClient.class);
  private static final TasksServlet servlet =
      new TasksServlet(authenticationVerifier, tasksClientFactory);

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

  private HttpServletRequest request;
  private HttpServletResponse response;
  private StringWriter stringWriter;
  private PrintWriter printWriter;

  // Tasks must be returned in order of retrieval - JSON includes tasks in desired order
  private static final String TASK_TITLE_ONE = "task one";
  private static final String TASK_TITLE_TWO = "task two";
  private static final String TASK_TITLE_THREE = "task three";
  private static final String TASK_TITLE_FOUR = "task four";
  private static final String TASK_ONE_TWO_JSON =
      String.format("[{\"title\":\"%s\"},{\"title\":\"%s\"}]", TASK_TITLE_ONE, TASK_TITLE_TWO);
  private static final String TASK_ALL_JSON =
      String.format(
          "[{\"title\":\"%s\"},{\"title\":\"%s\"},{\"title\":\"%s\"},{\"title\":\"%s\"}]",
          TASK_TITLE_ONE, TASK_TITLE_TWO, TASK_TITLE_THREE, TASK_TITLE_FOUR);
  private static final String EMPTY_JSON = "[]";
  private static final List<TaskList> noTaskLists = new ArrayList<>();
  private static final List<TaskList> someTaskLists = Arrays.asList(new TaskList(), new TaskList());
  private static final List<Task> noTasks = new ArrayList<>();
  private static final List<Task> tasksOneTwo =
      Arrays.asList(new Task().setTitle(TASK_TITLE_ONE), new Task().setTitle(TASK_TITLE_TWO));
  private static final List<Task> tasksThreeFour =
      Arrays.asList(new Task().setTitle(TASK_TITLE_THREE), new Task().setTitle(TASK_TITLE_FOUR));

  @BeforeClass
  public static void classInit() throws GeneralSecurityException, IOException {
    Mockito.when(tasksClientFactory.getTasksClient(Mockito.any())).thenReturn(tasksClient);
    // Authentication will always pass
    Mockito.when(authenticationVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);
  }

  @Before
  public void setUp() throws IOException {
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
    Mockito.when(tasksClient.listTaskLists()).thenReturn(noTaskLists);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(EMPTY_JSON));
  }

  @Test
  public void emptyTaskLists() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(someTaskLists);
    Mockito.when(tasksClient.listTasks(Mockito.any())).thenReturn(noTasks);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(EMPTY_JSON));
  }

  @Test
  public void oneEmptyTaskList() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(someTaskLists);
    Mockito.when(tasksClient.listTasks(Mockito.any())).thenReturn(noTasks).thenReturn(tasksOneTwo);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(TASK_ONE_TWO_JSON));
  }

  @Test
  public void completeTaskList() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(someTaskLists);
    Mockito.when(tasksClient.listTasks(Mockito.any()))
        .thenReturn(tasksOneTwo)
        .thenReturn(tasksThreeFour);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(TASK_ALL_JSON));
  }
}

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
/*
import static org.mockito.Mockito.when;

import com.google.api.services.gmail.model.Message;
import com.google.maps.DirectionsClient;
import com.google.maps.DirectionsClientFactory;
import com.google.sps.servlets.DirectionsServlet;
import com.google.sps.servlets.GmailServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * Test Gmail Servlet to ensure response contains correctly parsed messageIds. Assumes
 * AuthenticatedHttpServlet is functioning properly (those tests will fail otherwise).
 *//*
@RunWith(JUnit4.class)
public final class DirectionsServletTest {
  private static final DirectionsClientFactory directionsClientFactory =
      Mockito.mock(DirectionsClientFactory.class);
  private static final DirectionsClient directionsClient = Mockito.mock(DirectionsClient.class);
  private static final DirectionsServlet servlet =
      new DirectionsServlet(directionsClientFactory);

  private static final String API_KEY = "sampleApiKey";

  private static HttpServletRequest request;
  private HttpServletResponse response;
  private static StringWriter stringWriter;
  private static PrintWriter printWriter;

  private static final String MESSAGE_ID_ONE = "messageIdOne";
  private static final String MESSAGE_ID_TWO = "messageIdTwo";
  private static final String MESSAGE_ID_THREE = "messageIdThree";
  private static final List<Leg> 
  private static final String WINDSOR_TO_MONTREAL_WITH_WAYPOINTS_JSON = "[\"[DirectionsLeg: \"Windsor, ON, Canada\" -\u003e \"Waterloo, ON, Canada\" (42.31486680,-83.03656800 -\u003e 43.46430180,-80.52042120), duration\u003d2 hours 58 mins, distance\u003d293 km: 16 steps]","[DirectionsLeg: \"Waterloo, ON, Canada\" -\u003e \"Markham, ON, Canada\" (43.46430180,-80.52042120 -\u003e 43.85644940,-79.33771690), duration\u003d1 hour 14 mins, distance\u003d123 km: 15 steps]\",\"[DirectionsLeg: \"Markham, ON, Canada\" -\u003e \"Quebec City, QC, Canada\" (43.85644940,-79.33771690 -\u003e 46.81380600,-71.20822600), duration\u003d7 hours 42 mins, distance\u003d790 km: 30 steps]\",\"[DirectionsLeg: \"Quebec City, QC, Canada\" -\u003e \"Montreal, QC, Canada\" (46.81380600,-71.20822600 -\u003e 45.50171230,-73.56721840), duration\u003d2 hours 46 mins, distance\u003d253 km: 29 steps]\"]";
  private static final String MONTREAL_TO_WATERLOO_NO_WAYPOINTS_JSON = "[\"[DirectionsLeg: \"Montreal, QC, Canada\" -\u003e \"Waterloo, ON, Canada\" (45.50171230,-73.56721840 -\u003e 43.46430180,-80.52042120), duration\u003d6 hours 11 mins, distance\u003d638 km: 22 steps]\"]";
  private static final String MONTREAL_TO_MONTREAL_WITH_WAYPOINTS_JSON = "[\"[DirectionsLeg: \"Montreal, QC, Canada\" -\u003e \"Kitchener, ON, Canada\" (45.50171230,-73.56721840 -\u003e 43.45184990,-80.49313410), duration\u003d6 hours 10 mins, distance\u003d632 km: 16 steps]\",\"[DirectionsLeg: \"Kitchener, ON, Canada\" -\u003e \"Waterloo, ON, Canada\" (43.45184990,-80.49313410 -\u003e 43.46430180,-80.52042120), duration\u003d8 mins, distance\u003d3.0 km: 5 steps]\",\"[DirectionsLeg: \"Waterloo, ON, Canada\" -\u003e \"Windsor, ON, Canada\" (43.46430180,-80.52042120 -\u003e 42.31486680,-83.03656800), duration\u003d2 hours 55 mins, distance\u003d291 km: 18 steps]\",\"[DirectionsLeg: \"Windsor, ON, Canada\" -\u003e \"Montreal, QC, Canada\" (42.31486680,-83.03656800 -\u003e 45.50171230,-73.56721840), duration\u003d8 hours 42 mins, distance\u003d897 km: 21 steps]\"]";

  @BeforeClass
  public static void classInit() throws GeneralSecurityException, IOException {
    when(directionsClientFactory.getDirectionsClient(Mockito.any())).thenReturn(directionsClient);
  }

  @Before
  public void init() throws IOException {
    // Writer used in get/post requests to capture HTTP response values
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void clear() {
    // Dump contents after each test
    stringWriter.getBuffer().setLength(0);
  }

  @Test
  public void montrealToMontrealWithWaypoints() throws IOException {
    
    when(directionsClient.getDirections(Mockito.anyString(), Mockito.anyString(), Mockito.any(String[].class))).thenReturn(null);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(NO_MESSAGES_JSON));
  }

  @Test
  public void montrealToWaterlooWithNoWaypoints() throws IOException {
    when(gmailClient.listUserMessages(Mockito.anyString())).thenReturn(threeMessages);
    servlet.doGet(request, response);
    printWriter.flush();
    Assert.assertTrue(stringWriter.toString().contains(THREE_MESSAGES_JSON));
  }


}
*/
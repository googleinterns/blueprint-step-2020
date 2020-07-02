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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

/** Utility class for the Distance Matrix API. */
public final class DistanceMatrixUtility {

  /** Prevents instances of this class. */
  private DistanceMatrixUtility() {}

  /**
   * Sets multiple values to a single parameter.
   *
   * @param parameterName A string containing the name of parameter to set multiple values to.
   * @param parameterValues A list of string containing the multiple values to be set.
   * @return A list of NameValuePair to set as parameters on a URIBuilder.
   */
  private static List<NameValuePair> buildParameters(
      String parameterName, List<String> parameterValues) {
    List<NameValuePair> parameters = new ArrayList<>();
    for (String parameterValue : parameterValues) {
      parameters.add(new BasicNameValuePair(parameterName, parameterValue));
    }
    return parameters;
  }

  /**
   * Builds custom URI to send a HTTP GET Request to the Distance Matrix API.
   *
   * @param destinations A list of string containing the destinations to build a distance matrix to.
   * @param origin A string representing the origin to build a distance matrix from.
   * @return A string representing the URI.
   * @throws URISyntaxException Indicates that the built string could not be parsed as a URI.
   */
  public static String getDistanceMatrixUri(List<String> destinations, String origin, String apiKey)
      throws URISyntaxException {
    URI uri =
        new URIBuilder()
            .setScheme("https")
            .setHost("maps.googleapis.com")
            .setPath("/maps/api/distancematrix/json")
            .setParameters(buildParameters("destinations", destinations))
            .setParameter("origins", origin)
            .setParameter("units", "imperial")
            .setParameter("key", apiKey)
            .build();
    return uri.toString();
  }

  /**
   * Gets the result of a call to the Distance Matrix API.
   *
   * @param destinations A list of string containing the destinations to build a distance matrix to.
   * @param origin A string representing the origin to build a distance matrix from.
   * @return A JSONObject representing the result from a call to the Distance Matrix API.
   * @throws IOException Indicates failed or interrupted I/O operations.
   * @throws JSONException Indicates a problem with the JSON API.
   * @throws URISyntaxException Indicates that the built string could not be parsed as a URI.
   */
  public static JSONObject getDistanceMatrix(
      List<String> destinations, String origin, String apiKey)
      throws IOException, JSONException, URISyntaxException {
    String uri = getDistanceMatrixUri(destinations, origin, apiKey);
    String jsonString = Request.Get(uri).execute().returnContent().toString();
    return new JSONObject(jsonString);
  }
}

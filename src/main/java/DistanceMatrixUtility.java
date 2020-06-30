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
import java.util.List;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

public final class DistanceMatrixUtility {

  // private final String API_KEY = "AIzaSyBwtyuDDyUts62GMDNBMZOEBLyaS9F80V4";

  public static String getDistanceMatrix() throws IOException {

    String content = Request.Get("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=University+of+Waterloo&destinations=Wilfrid+Laurier+University&key=AIzaSyBwtyuDDyUts62GMDNBMZOEBLyaS9F80V4")
    .execute().returnContent().toString();

    System.out.println(content);
    return content;
  }

}

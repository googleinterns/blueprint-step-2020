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

package com.google.sps.utility;

import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocationsUtility {
  /**
   * Parses for locations in Tasks.
   *
   * @param prefix String which represents the prefix wrapped in square brackets to look for. (e.g.
   *     Location if looking for [Location: ])
   * @param tasks List of tasks to parse for locations from.
   * @return List of strings representing the locations.
   */
  public static List<String> getLocations(String prefix, List<Task> tasks) {
    return tasks.stream()
        .map(task -> task.getNotes())
        .filter(Objects::nonNull)
        .map(notes -> getLocation(prefix, notes))
        .filter(place -> !place.equals("No " + prefix))
        .collect(Collectors.toList());
  }

  /**
   * Parses for string enclosed in [prefix: ] in notes of a task
   *
   * @param prefix String to look for in the square brackets
   * @param taskNotes String to parse from, usually in the form "... [prefix: ... ] ..."
   * @return String enclosed in [prefix: ] or No prefix if no enclosure found
   */
  private static String getLocation(String prefix, String taskNotes) {
    // Regular expression matches the characters [prefix: and ] literally, (.*?) signifies the 1st
    // capturing group which matches any character except for line terminators
    String regex = "\\[" + prefix + ": (.*?)\\]";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(taskNotes);
    if (matcher.find()) {
      // Return the 1st captured group obtained from executing the regular expression
      return matcher.group(1);
    }
    return "No " + prefix;
  }
  
  public static void generateCombinations(List<List<String>> lists, List<List<String>> result, int depth, List<String> current) {
    if (depth == lists.size()) {
        result.add(new ArrayList<>(current));
        return;
    }
    for (int i = 0; i < lists.get(depth).size(); i++) {
      List<String> next = new ArrayList<>();
      next.addAll(current);
      next.add(lists.get(depth).get(i));
      generateCombinations(lists, result, depth + 1, next);
    }
  }
}

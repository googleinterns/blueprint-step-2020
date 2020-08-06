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

public final class LocationsUtility {

  private LocationsUtility() {}

  /**
   * Parses for locations in Tasks. Missing, empty and duplicate locations are ignored.
   *
   * @param prefix String which represents the prefix wrapped in square brackets to look for. (e.g.
   *     Location if looking for [Location: ])
   * @param tasks List of tasks to parse for locations from.
   * @return List of strings representing the locations.
   */
  public static List<String> getLocations(String prefix, List<Task> tasks) {
    // Regular expression matches the characters [prefix: and ] literally, (.*?) signifies the 1st
    // capturing group which matches any character except for line terminators
    String regex = "\\[" + prefix + ": (.*?)\\]";
    Pattern pattern = Pattern.compile(regex);
    return tasks.stream()
        .map(task -> task.getNotes())
        .filter(Objects::nonNull)
        .map(notes -> getLocation(pattern, prefix, notes))
        .filter(place -> !place.equals(""))
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Parses for string enclosed in [prefix: ] in notes of a task. Only the 1st match is returned.
   * All other matches are ignored.
   *
   * @param prefix String to look for in the square brackets
   * @param taskNotes String to parse from, usually in the form "... [prefix: ... ] ..."
   * @return String enclosed in [prefix: ] or an empty string if no enclosure found
   */
  private static String getLocation(Pattern pattern, String prefix, String taskNotes) {
    Matcher matcher = pattern.matcher(taskNotes);
    if (matcher.find()) {
      // Return the 1st captured group obtained from executing the regular expression
      return matcher.group(1);
    }
    return "";
  }

  /**
   * Generates combinations of the lists and appends them to the result parameter.
   *
   * @param lists A list of lists to generate combinations for.
   * @param result A pointer to a list to contain the generated combinations.
   * @param current A list containing the combination built so far at the recursive call.
   */
  private static void generateCombinationsHelper(
      List<List<String>> lists, List<List<String>> result, List<String> current) {
    int depth = current.size();
    if (depth == lists.size()) {
      result.add(new ArrayList<>(current));
      return;
    }
    for (int i = 0; i < lists.get(depth).size(); i++) {
      current.add(lists.get(depth).get(i));
      generateCombinationsHelper(lists, result, current);
      current.remove(depth);
    }
  }

  /**
   * Generates all combinations of the lists recursively.
   *
   * @param lists A list of lists to generate combinations for.
   */
  public static List<List<String>> generateCombinations(List<List<String>> lists) {
    List<List<String>> combinations = new ArrayList<>();
    generateCombinationsHelper(lists, combinations, new ArrayList<>());
    return combinations;
  }
}

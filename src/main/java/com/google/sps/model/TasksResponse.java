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

package com.google.sps.model;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.Builder;

/**
 * Contains the summary tasks information that should be passed to the client, as well as the
 * methods to generate these statistics.
 */
@Builder
public final class TasksResponse {
  @Builder.Default Map<String, String> taskListIdsAndTitles = new HashMap<String, String>();
  @Builder.Default long tasksToCompleteCount = 0;
  @Builder.Default long tasksDueTodayCount = 0;
  @Builder.Default long tasksCompletedTodayCount = 0;
  @Builder.Default long tasksOverdueCount = 0;

  @Override
  public boolean equals(final Object object) {
    if (object == null || object.getClass() != getClass()) {
      return false;
    }

    if (object == this) {
      return true;
    }

    TasksResponse other = (TasksResponse) object;

    return ((this.taskListIdsAndTitles).equals(other.taskListIdsAndTitles)
        && this.tasksToCompleteCount == other.tasksToCompleteCount
        && this.tasksDueTodayCount == other.tasksDueTodayCount
        && this.tasksCompletedTodayCount == other.tasksCompletedTodayCount
        && this.tasksOverdueCount == other.tasksOverdueCount);
  }
}

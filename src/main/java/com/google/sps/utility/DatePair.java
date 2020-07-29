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

import java.util.*;

/** Class implementing a Pair Data Structure. */
public final class DatePair {

  private final Date key;
  private final Date value;

  /**
   * Constructor for the class
   *
   * @param key the start time of the interval
   * @param value the end time of the interval
   */
  public DatePair(Date key, Date value) {
    this.key = key;
    this.value = value;
  }
  
  public Date getKey() {
    return this.key;
  }

  public Date getValue() {
    return this.value;
  }
}
